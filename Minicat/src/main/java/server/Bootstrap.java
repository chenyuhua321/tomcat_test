package server;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServlet;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * Minicat的主类
 */
public class Bootstrap {

    /**定义socket监听的端口号*/
    private int port = 8080;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }


    /**
     * Minicat启动需要初始化展开的一些操作
     */
    public void start() throws Exception {

        // 加载解析相关的配置，web.xml
        loadServlet();


        // 定义一个线程池
        int corePoolSize = 10;
        int maximumPoolSize =50;
        long keepAliveTime = 100L;
        TimeUnit unit = TimeUnit.SECONDS;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(50);
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();


        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                corePoolSize,
                maximumPoolSize,
                keepAliveTime,
                unit,
                workQueue,
                threadFactory,
                handler
        );





        /*
            完成Minicat 1.0版本
            需求：浏览器请求http://localhost:8080,返回一个固定的字符串到页面"Hello Minicat!"
         */
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("=====>>>Minicat start on port：" + port);

        /*while(true) {
            Socket socket = serverSocket.accept();
            // 有了socket，接收到请求，获取输出流
            OutputStream outputStream = socket.getOutputStream();
            String data = "Hello Minicat!";
            String responseText = HttpProtocolUtil.getHttpHeader200(data.getBytes().length) + data;
            outputStream.write(responseText.getBytes());
            socket.close();
        }*/


        /**
         * 完成Minicat 2.0版本
         * 需求：封装Request和Response对象，返回html静态资源文件
         */
        /*while(true) {
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();

            // 封装Request对象和Response对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());

            response.outputHtml(request.getUrl());
            socket.close();

        }*/


        /**
         * 完成Minicat 3.0版本
         * 需求：可以请求动态资源（Servlet）
         */
        /*while(true) {
            Socket socket = serverSocket.accept();
            InputStream inputStream = socket.getInputStream();

            // 封装Request对象和Response对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());

            // 静态资源处理
            if(servletMap.get(request.getUrl()) == null) {
                response.outputHtml(request.getUrl());
            }else{
                // 动态资源servlet请求
                HttpServlet httpServlet = servletMap.get(request.getUrl());
                httpServlet.service(request,response);
            }

            socket.close();

        }
*/

        /*
            多线程改造（不使用线程池）
         */
        /*while(true) {
            Socket socket = serverSocket.accept();
            RequestProcessor requestProcessor = new RequestProcessor(socket,servletMap);
            requestProcessor.start();
        }*/



        System.out.println("=========>>>>>>使用线程池进行多线程改造");
        /*
            多线程改造（使用线程池）
         */
        while(true) {

            Socket socket = serverSocket.accept();
            RequestProcessor requestProcessor = new RequestProcessor(socket,mapperHosts);
            //requestProcessor.start();
            threadPoolExecutor.execute(requestProcessor);
        }



    }


    private Map<String, HttpServlet> servletMap = new HashMap<String,HttpServlet>();

    private MapperHost[] mapperHosts = new MapperHost[1];

    /**
     * 加载解析web.xml，初始化Servlet
     */
    private void loadServlet() {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("server.xml");
        SAXReader saxReader = new SAXReader();

        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();
            Element server = (Element)rootElement.selectSingleNode("//Server");
            List<Element> serviceList = server.selectNodes("//Service");
            for (int i = 0; i < serviceList.size(); i++) {
                Element service = serviceList.get(i);
                Element connector = (Element)service.selectSingleNode("//Connector");
                String port = connector.attributeValue("port");
                if(Objects.nonNull(port) && port.length() >0) {
                    this.port = Integer.valueOf(port);
                }
                Element engine = (Element)service.selectSingleNode("//Engine");
                Element host = (Element)engine.selectSingleNode("//Host");
                String hostName = host.attributeValue("name");
                String hostAppBase = host.attributeValue("appBase");

                List<Element> contexts = host.selectNodes("//Context");
                MapperContext[] mapperContexts = new MapperContext[contexts.size()];
                for (int j = 0; j < contexts.size(); j++) {
                    Element element =  contexts.get(j);
                    String contextPath = element.attributeValue("path");
                    String docBase = element.attributeValue("docBase");
                    File webxml = new File(hostAppBase+"\\"+docBase+"\\WEB-INF\\web.xml");
                    InputStream contextWeb = new FileInputStream(webxml);
                    SAXReader contextReader = new SAXReader();
                    Document  contextdocument = contextReader.read(contextWeb);
                    Element contextRootElement = contextdocument.getRootElement();

                    List<Element> selectNodes = contextRootElement.selectNodes("//servlet");
                    MapperWrapper[] mapperWrappers = new MapperWrapper[selectNodes.size()];

                    for (int k = 0; k < selectNodes.size(); k++) {
                        Element contextServlet =  selectNodes.get(i);
                        // <servlet-name>lagou</servlet-name>
                        Element servletnameElement = (Element) contextServlet.selectSingleNode("servlet-name");
                        String servletName = servletnameElement.getStringValue();
                        // <servlet-class>server.LagouServlet</servlet-class>
                        Element servletclassElement = (Element) contextServlet.selectSingleNode("servlet-class");
                        String servletClass = servletclassElement.getStringValue();
                        String servletPath = servletClass.replace(".", "\\");

                        // 根据servlet-name的值找到url-pattern
                        Element servletMapping = (Element) contextRootElement.selectSingleNode("/web-app/servlet-mapping[servlet-name='" + servletName + "']");
                        // /lagou
                        String urlPattern = servletMapping.selectSingleNode("url-pattern").getStringValue();
                        MyClassLoader myClassLoader = new MyClassLoader(hostAppBase+"\\"+docBase+"\\WEB-INF\\classes\\"+servletPath+".class");

                        //加载Log这个class文件
                        Class<?> Log = myClassLoader.loadClass(servletClass);
                        mapperWrappers[k] = new MapperWrapper(urlPattern,(HttpServlet) Log.newInstance());
                    }
                    mapperContexts[j]= new MapperContext(contextPath,mapperWrappers);
                }
                mapperHosts[i]=new MapperHost(hostName,mapperContexts);
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * Minicat 的程序启动入口
     * @param args
     */
    public static void main(String[] args) {
        Bootstrap bootstrap = new Bootstrap();
        try {
            // 启动Minicat
            bootstrap.start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
