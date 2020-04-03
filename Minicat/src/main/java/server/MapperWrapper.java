package server;

import javax.servlet.http.HttpServlet;

/**
 * @author Chenyuhua
 * @date 2020/4/3 2:31
 */
public class MapperWrapper extends MapElement<HttpServlet>{
    public MapperWrapper(String name, HttpServlet object) {
        super(name, object);
    }
}
