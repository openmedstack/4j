package org.openmedstack.domain;

import java.util.concurrent.CompletableFuture;

public interface ISaveSnapshots {
    <T extends Memento> CompletableFuture<Boolean> save(AggregateRootBase<T> aggregateRoot);
}
