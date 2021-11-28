package org.openmedstack.domain.guice

import org.openmedstack.domain.Memento
import org.openmedstack.eventstore.*
import java.time.Clock
import java.time.Instant
import java.util.*
import java.util.concurrent.CompletableFuture

class OptimisticEventStream private constructor(private val _bucketId: String?, private val _streamId: String, private val _persistence: ICommitEvents) : IEventStream {
    private val _committed = ArrayList<EventMessage>()
    private val _events = ArrayList<EventMessage>()
    private val _identifiers = HashSet<UUID>()
    private val _committedHeaders = HashMap<String, Any>()
    private val _unCommittedHeaders = HashMap<String, Any>()
    private var _streamRevision = 0
    private var _commitSequence = 0
    private var _disposed: Boolean = false
    private fun populateStream(minRevision: Int, maxRevision: Int, commits: Iterable<Commit>) {
        for (commit in commits) {
            _identifiers.add(commit.commitId)
            _commitSequence = commit.commitSequence
            val currentRevision = commit.streamRevision - commit.events.size + 1
            if (currentRevision > maxRevision) {
                return
            }
            copyToCommittedHeaders(commit)
            copyToEvents(minRevision, maxRevision, currentRevision, commit)
        }
    }

    private fun copyToCommittedHeaders(commit: Commit) {
        for (key in commit.headers.keys) {
            committedHeaders[key] = commit.headers[key]!!
        }
    }

    private fun copyToEvents(minRevision: Int, maxRevision: Int, currentRevision: Int, commit: Commit) {
        for (evt in commit.events) {
            if (currentRevision > maxRevision) {
                break
            }

            if (currentRevision + 1 < minRevision) {
                continue
            }
            _committed.add(evt)
            _streamRevision = currentRevision
        }
    }

    override val bucketId: String?
        get() = _bucketId

    override val streamId: String
        get() = _streamId

    override val streamRevision: Int
        get() = _streamRevision

    override val commitSequence: Int
        get() = _commitSequence

    override val committedEvents: List<EventMessage>
        get() = _committed

    override val committedHeaders: HashMap<String, Any>
        get() = _committedHeaders

    override val uncommittedEvents: List<EventMessage>
        get() = _events

    override val uncommittedHeaders: HashMap<String, Any>
        get() = _unCommittedHeaders

    override fun add(uncommittedEvent: EventMessage) {
        _events.add(uncommittedEvent)
    }

    private fun hasChanges(): Boolean {
        return when {
            _disposed -> {
                false
            }
            else -> _events.size > 0
        }
    }

    @Throws(DuplicateCommitException::class)
    override fun commitChanges(commitId: UUID): CompletableFuture<*> {
        if (_identifiers.contains(commitId)) {
            throw DuplicateCommitException(String.format("Duplicate commit of %s", commitId))
        }
        return if (!hasChanges()) {
            CompletableFuture.completedFuture(true)
        } else persistChanges(commitId)
    }

    private fun persistChanges(commitId: UUID): CompletableFuture<Void> {
        val attempt = buildCommitAttempt(commitId)
        return _persistence.commit(attempt).thenAccept { commit: Commit? ->
            populateStream(
                    _streamRevision + 1,
                    attempt.streamRevision,
                    if (commit == null) java.util.List.of() else java.util.List.of(commit))
            clearChanges()
        }
    }

    private fun buildCommitAttempt(commitId: UUID): CommitAttempt {
        return CommitAttempt(
                _bucketId,
                _streamId,
                _streamRevision + _events.size,
                commitId,
                _commitSequence + 1,
                Instant.now(Clock.systemUTC()),
                uncommittedHeaders,
                _events)
    }

    override fun clearChanges() {
        _events.clear()
        _unCommittedHeaders.clear()
    }

    @Throws(Exception::class)
    override fun close() {
        if (_disposed) {
            return
        }
        _disposed = true
    }

    companion object {
        fun create(bucketId: String?, streamId: String, persistence: ICommitEvents): OptimisticEventStream {
            return OptimisticEventStream(bucketId, streamId, persistence)
        }

        fun create(
                bucketId: String?,
                streamId: String,
                persistence: ICommitEvents,
                minRevision: Int,
                maxRevision: Int): CompletableFuture<IEventStream> {
            return persistence.getFrom(bucketId, streamId, minRevision, maxRevision)
                    .thenApply { commits: Iterable<Commit> ->
                        try {
                            return@thenApply populateStream(bucketId, streamId, persistence, minRevision, maxRevision, commits)
                        } catch (e: StreamNotFoundException) {
                            return@thenApply null
                        }
                    }
        }

        @Throws(StreamNotFoundException::class)
        private fun populateStream(bucketId: String?, streamId: String, persistence: ICommitEvents, minRevision: Int, maxRevision: Int, commits: Iterable<Commit>): IEventStream {
            val instance = create(bucketId, streamId, persistence)
            instance.populateStream(minRevision, maxRevision, commits)
            if (minRevision > 0 && instance._committed.size == 0) {
                throw StreamNotFoundException(String.format("Stream %s in bucket %s not found", streamId, instance._bucketId))
            }
            return instance
        }

        fun <TMemento : Memento> create(
                snapshot: Snapshot<TMemento>,
                persistence: ICommitEvents,
                maxRevision: Int): CompletableFuture<IEventStream> {
            return persistence.getFrom(
                    snapshot.bucketId,
                    snapshot.streamId,
                    snapshot.streamRevision,
                    maxRevision)
                    .thenApply { commits: Iterable<Commit> ->
                        val instance = create(snapshot.bucketId, snapshot.streamId, persistence)
                        instance.populateStream(snapshot.streamRevision + 1, maxRevision, commits)
                        instance._streamRevision = snapshot.streamRevision + instance._committed.size
                        instance
                    }
        }
    }
}
