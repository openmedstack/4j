package org.openmedstack.eventstore;

import org.openmedstack.domain.Aggregate;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.function.Consumer;

public interface Repository {
    <TAggregate extends Aggregate> CompletableFuture<TAggregate> getById(Class<TAggregate> type, String id);

    <TAggregate extends Aggregate> CompletableFuture<TAggregate> getById(Class<TAggregate> type,String id, int version);

    CompletableFuture<Boolean> save(Aggregate aggregate, Consumer<HashMap<String, Object>> updateHeaders) throws DuplicateCommitException;
}
