package org.openmedstack.eventstore

import java.util.concurrent.CompletableFuture

interface ICommitEvents {
    fun getFrom(bucketId: String?, streamId: String, minRevision: Int, maxRevision: Int): CompletableFuture<Iterable<Commit>>
    fun commit(attempt: CommitAttempt): CompletableFuture<Commit>
}
