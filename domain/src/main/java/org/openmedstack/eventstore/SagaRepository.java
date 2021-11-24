package org.openmedstack.eventstore;

import org.openmedstack.domain.Saga;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface SagaRepository {
    <TSaga extends Saga> CompletableFuture<TSaga> getById(Class<TSaga> type, String sagaId);

    CompletableFuture<Boolean> save(Saga saga, Consumer<HashMap<String, Object>> updateHeaders);
}
