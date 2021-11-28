package org.openmedstack.eventstore

import java.time.Instant
import java.util.*

class CommitAttempt(
        private val _bucketId: String?,
        private val _streamId: String,
        private val _streamRevision: Int,
        private val _commitId: UUID,
        private val _commitSequence: Int,
        private val _commitStamp: Instant,
        private val _headers: HashMap<String, Any>,
        private val _events: MutableList<EventMessage>) {
    val bucketId: String?
        get() = _bucketId

    val streamId: String
        get() = _streamId

    val streamRevision: Int
        get() = _streamRevision

    val commitId: UUID
        get() = _commitId

    val commitSequence: Int
        get() = _commitSequence

    val commitStamp: Instant
        get() = _commitStamp

    val headers: HashMap<String, Any>
        get() = _headers

    val events: MutableList<EventMessage>
        get() = _events
}

class CommitImpl private constructor(
    private val _bucketId: String?,
    private val _streamId: String,
    private val _streamRevision: Int,
    private val _commitId: UUID,
    private val _commitSequence: Int,
    private val _commitStamp: Instant,
    private val _headers: HashMap<String, Any>,
    private val _events: MutableList<EventMessage>): Commit {
    override val bucketId: String?
        get() = _bucketId
    override val streamId: String
        get() = _streamId
    override val streamRevision: Int
        get() = _streamRevision
    override val commitId: UUID
        get() = _commitId
    override val commitSequence: Int
        get() = _commitSequence
    override val commitStamp: Instant
        get() = _commitStamp
    override val headers: HashMap<String, Any>
        get() = _headers
    override val events: MutableList<EventMessage>
        get() = _events
    override val checkpointToken: Long
        get() = 0

    companion object {
        fun create(attempt: CommitAttempt): Commit {
            return CommitImpl(
                attempt.bucketId,
                attempt.streamId,
                attempt.streamRevision,
                attempt.commitId,
                attempt.commitSequence,
                attempt.commitStamp,
                attempt.headers,
                attempt.events
            )
        }
    }
}
