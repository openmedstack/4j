package org.openmedstack.eventstore

import java.util.*
import java.util.concurrent.CompletableFuture

interface IEventStream : AutoCloseable {
    val bucketId: String?
    val streamId: String
    val streamRevision: Int
    val commitSequence: Int
    val committedEvents: List<EventMessage>
    val committedHeaders: HashMap<String, Any>
    val uncommittedEvents: List<EventMessage>
    val uncommittedHeaders: HashMap<String, Any>
    fun add(uncommittedEvent: EventMessage)
    @Throws(DuplicateCommitException::class, ConcurrencyException::class)
    fun commitChanges(commitId: UUID): CompletableFuture<*>
    fun clearChanges()
}