package server;

/**
 * @author Chenyuhua
 * @date 2020/4/3 2:27
 */
public abstract  class MapElement<T> {

    public final String name;
    public final T object;

    public MapElement(String name, T object) {
        this.name = name;
        this.object = object;
    }
}
