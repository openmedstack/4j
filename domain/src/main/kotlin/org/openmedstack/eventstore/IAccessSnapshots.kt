package org.openmedstack.eventstore

import org.openmedstack.domain.Memento
import java.util.concurrent.CompletableFuture

interface IAccessSnapshots {
    fun <TMemento : Memento> getSnapshot(type: Class<Snapshot<TMemento>>, bucketId: String?, streamId: String, maxRevision: Int): CompletableFuture<Snapshot<TMemento>?>
    fun <TMemento : Memento> addSnapshot(snapshot: Snapshot<TMemento>): CompletableFuture<Boolean>
}
