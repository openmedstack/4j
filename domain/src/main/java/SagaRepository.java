import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface SagaRepository {
    Future<? extends Saga> getById(String sagaId);

    Future<Boolean> save(Saga saga, Consumer<HashMap<String, Object>> updateHeaders);
}
