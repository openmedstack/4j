import java.util.function.Consumer;

public interface IRouteEvents {
    <T> void register(Consumer<T> handler);

    void dispatch(Object eventMessage);
}
