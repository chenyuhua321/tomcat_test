# Tomcat体系结构

## 一、Tomcat是什么

tomcat是一个http服务器，一个servlet容器
<img src="https://gitee.com/chenyuhua321/tomcat_test/blob/master/Image/image-20200403150321004.png"/>

核心组件：连接器Connector 和 Servlet容器Container

只有一个Server服务器

一个Server服务器有多个Service

一个容器可以有多个连接

连接器：
<img src="https://gitee.com/chenyuhua321/tomcat_test/blob/master/Image/image-20200403151857931.png"/>


EndPoint监听端口接收返回请求

Processor将请求转为Tomcat的Request和Response

Adapter将Request转为ServletRequest
<img src="https://gitee.com/chenyuhua321/tomcat_test/blob/master/Image/image-20200403151020049.png"/>

而容器的结构。

一个容器有一个Engine，虚拟引擎

一个Engine有多个host，虚拟主机

一个host有多个Context 就是我们的项目应用

wrapper 代表一个 Servlet，它负责管理一个 Servlet。

他们会层级load

然后层级start

