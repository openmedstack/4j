package org.openmedstack.domain;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.function.Consumer;

public interface Repository {
    <TAggregate extends Aggregate> CompletableFuture<TAggregate> getById(String id);

    <TAggregate extends Aggregate> CompletableFuture<TAggregate> getById(String id, Integer version);

    CompletableFuture<Boolean> save(Aggregate aggregate, Consumer<HashMap<String, Object>> updateHeaders);
}

