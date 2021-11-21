package org.openmedstack.domain;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface SagaRepository {
    CompletableFuture<? extends Saga> getById(String sagaId);

    CompletableFuture<Boolean> save(Saga saga, Consumer<HashMap<String, Object>> updateHeaders);
}
