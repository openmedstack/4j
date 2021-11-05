import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;

public interface Repository {
    <TAggregate extends Aggregate> Future<TAggregate> getById(String id);

    <TAggregate extends Aggregate> Future<TAggregate> getById(String id, Integer version);

    Future<Boolean> save(Aggregate aggregate, Consumer<HashMap<String, Object>> updateHeaders);
}
