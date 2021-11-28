package org.openmedstack.eventstore

import org.openmedstack.domain.Memento
import java.util.concurrent.CompletableFuture

interface IStoreEvents : AutoCloseable {
    fun createStream(bucketId: String?, streamId: String): CompletableFuture<IEventStream>
    fun openStream(bucketId: String?, streamId: String, minRevision: Int, maxRevision: Int): CompletableFuture<IEventStream>
    fun <TMemento : Memento> openStream(snapshot: Snapshot<TMemento>, maxRevision: Int): CompletableFuture<IEventStream>
}
