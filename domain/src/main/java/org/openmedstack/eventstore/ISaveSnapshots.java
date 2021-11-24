package org.openmedstack.eventstore;

import org.openmedstack.domain.AggregateRootBase;
import org.openmedstack.domain.Memento;

import java.util.concurrent.CompletableFuture;

public interface ISaveSnapshots {
    <T extends Memento> CompletableFuture<Boolean> save(AggregateRootBase<T> aggregateRoot);
}
