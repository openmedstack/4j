package org.openmedstack.eventstore

import org.openmedstack.domain.AggregateRootBase
import org.openmedstack.domain.Memento
import java.util.concurrent.CompletableFuture

interface ISaveSnapshots {
    fun <T : Memento> save(aggregateRoot: AggregateRootBase<T>): CompletableFuture<Boolean>
}
