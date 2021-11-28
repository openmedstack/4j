package org.openmedstack.eventstore

import org.openmedstack.domain.Aggregate
import org.openmedstack.domain.Memento
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

interface Repository {
    fun <TAggregate : Aggregate, TMemento : Memento> getById(type: Class<TAggregate>, mementoType: Class<Snapshot<TMemento>>, id: String): CompletableFuture<TAggregate>
    fun <TAggregate : Aggregate, TMemento : Memento> getById(type: Class<TAggregate>, mementoType: Class<Snapshot<TMemento>>, id: String, version: Int): CompletableFuture<TAggregate>
    @Throws(DuplicateCommitException::class)
    fun <TAggregate : Aggregate, TMemento : Memento> save(aggregate: TAggregate, updateHeaders: Consumer<HashMap<String, Any>>): CompletableFuture<Boolean>
}
