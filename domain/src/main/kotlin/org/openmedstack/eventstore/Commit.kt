package org.openmedstack.eventstore

import java.time.Instant
import java.util.*

interface Commit {
    val bucketId: String?
    val streamId: String
    val streamRevision: Int
    val commitId: UUID
    val commitSequence: Int
    val commitStamp: Instant
    val headers: HashMap<String, Any>
    val events: MutableList<EventMessage>
    val checkpointToken: Long
}
