package server;

import javax.servlet.http.HttpServlet;
import java.io.InputStream;
import java.net.Socket;

public class RequestProcessor extends Thread {

    private Socket socket;
    private MapperHost[] mapperHosts;

    public RequestProcessor(Socket socket, MapperHost[] mapperHosts) {
        this.socket = socket;
        this.mapperHosts = mapperHosts;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();

            // 封装Request对象和Response对象
            Request request = new Request(inputStream);
            Response response = new Response(socket.getOutputStream());
            String url = request.getUrl();
            String contextName = url.substring(0, url.substring(1, url.length()).indexOf("/") + 1);
            String wrapperName = url.substring(url.substring(1,url.length()).indexOf("/") + 1, url.length());
            // 静态资源处理
            for (MapperHost mapperHost : mapperHosts) {
                if ("localhost".equals(mapperHost.name)) {
                    MapperContext[] mapperContexts = mapperHost.object;
                    for (MapperContext mapperContext : mapperContexts) {
                        if (contextName.equals(mapperContext.name)) {
                            MapperWrapper[] mapperWrappers = mapperContext.object;
                            for (MapperWrapper mapperWrapper : mapperWrappers) {
                                if (wrapperName.equals(mapperWrapper.name)) {
                                    HttpServlet httpServlet = mapperWrapper.object;
                                    httpServlet.service(request, response);
                                    socket.close();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            response.outputHtml(request.getUrl());
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
