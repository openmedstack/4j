package org.openmedstack.domain.guice;

import org.openmedstack.eventstore.*;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class OptimisticEventStream implements IEventStream {
    private final ArrayList<EventMessage> _committed = new ArrayList<>();
    private final ArrayList<EventMessage> _events = new ArrayList<>();
    private final HashSet<UUID> _identifiers = new HashSet<>();
    private final HashMap<String, Object> _committedHeaders = new HashMap<>();
    private final HashMap<String, Object> _unCommittedHeaders = new HashMap<>();
    private final ICommitEvents _persistence;
    private final String _bucketId;
    private final String _streamId;
    private int _streamRevision = 0;
    private int _commitSequence = 0;
    private Boolean _disposed;

    private OptimisticEventStream(String bucketId, String streamId, ICommitEvents persistence) {
        _bucketId = bucketId;
        _streamId = streamId;
        _persistence = persistence;
    }

    public static OptimisticEventStream Create(String bucketId, String streamId, ICommitEvents persistence) {
        return new OptimisticEventStream(bucketId, streamId, persistence);
    }

    public static CompletableFuture<IEventStream> Create(
            String bucketId,
            String streamId,
            ICommitEvents persistence,
            int minRevision,
            int maxRevision) {
        return persistence.getFrom(bucketId, streamId, minRevision, maxRevision)
                .thenApply(commits -> {
                    try {
                        return populateStream(bucketId, streamId, persistence, minRevision, maxRevision, commits);
                    } catch (StreamNotFoundException e) {
                        return null;
                    }
                });
    }

    private static IEventStream populateStream(String bucketId, String streamId, ICommitEvents persistence, int minRevision, int maxRevision, Iterable<Commit> commits) throws StreamNotFoundException{
        var instance = Create(bucketId, streamId, persistence);
        instance.populateStream(minRevision, maxRevision, commits);

        if (minRevision > 0 && instance._committed.size() == 0) {
            throw new StreamNotFoundException(
                    String.format("Stream %s in bucket %s not found", streamId, instance._bucketId));
        }

        return instance;
    }

    public static CompletableFuture<IEventStream> Create(
            Snapshot snapshot,
            ICommitEvents persistence,
            int maxRevision) {
        return persistence.getFrom(
                        snapshot.getBucketId(),
                        snapshot.getStreamId(),
                        snapshot.getStreamRevision(),
                        maxRevision)
                .thenApply(commits -> {
                    var instance = Create(snapshot.getBucketId(), snapshot.getStreamId(), persistence);
                    instance.populateStream(snapshot.getStreamRevision() + 1, maxRevision, commits);
                    instance._streamRevision = snapshot.getStreamRevision() + instance._committed.size();

                    return instance;
                });
    }

    private void populateStream(int minRevision, int maxRevision, Iterable<Commit> commits) {
        if (commits == null) {
            return;
        }
        for (var commit : commits) {
            _identifiers.add(commit.getCommitId());

            _commitSequence = commit.getCommitSequence();
            var currentRevision = commit.getStreamRevision() - commit.getEvents().size() + 1;
            if (currentRevision > maxRevision) {
                return;
            }

            CopyToCommittedHeaders(commit);
            CopyToEvents(minRevision, maxRevision, currentRevision, commit);
        }
    }

    private void CopyToCommittedHeaders(Commit commit) {
        for (var key : commit.getHeaders().keySet()) {
            getCommittedHeaders().put(key, commit.getHeaders().get(key));
        }
    }

    private void CopyToEvents(int minRevision, int maxRevision, int currentRevision, Commit commit) {
        for (var evt : commit.getEvents()) {
            if (currentRevision > maxRevision) {
                break;
            }

            if (currentRevision++ < minRevision) {
                continue;
            }

            _committed.add(evt);
            _streamRevision = currentRevision - 1;
        }
    }

    @Override
    public String BucketId() {
        return _bucketId;
    }

    @Override
    public String getStreamId() {
        return _streamId;
    }

    @Override
    public int getStreamRevision() {
        return _streamRevision;
    }

    @Override
    public int getCommitSequence() {
        return _commitSequence;
    }

    @Override
    public List<EventMessage> getCommittedEvents() {
        return _committed;
    }

    @Override
    public HashMap<String, Object> getCommittedHeaders() {
        return _committedHeaders;
    }

    @Override
    public List<EventMessage> getUncommittedEvents() {
        return _events;
    }

    @Override
    public HashMap<String, Object> getUncommittedHeaders() {
        return _unCommittedHeaders;
    }

    @Override
    public void add(EventMessage uncommittedEvent) {
        if (uncommittedEvent == null || uncommittedEvent.getBody() == null) {
            throw new IllegalArgumentException();
        }

        _events.add(uncommittedEvent);
    }

    private Boolean HasChanges()
    {
        if (_disposed)
        {
            return false;
        }

        if (_events.size() > 0)
        {
            return true;
        }

        return false;
    }

    @Override
    public CompletableFuture commitChanges(UUID commitId) throws DuplicateCommitException {
        if (_identifiers.contains(commitId)) {
            throw new DuplicateCommitException(String.format("Duplicate commit of %s", commitId));
        }

        if (!HasChanges()) {
            return CompletableFuture.completedFuture(true);
        }

        return persistChanges(commitId);
    }

    private CompletableFuture<Void> persistChanges(UUID commitId) {
        var attempt = buildCommitAttempt(commitId);

        return _persistence.commit(attempt).thenAccept(commit -> {

            populateStream(
                    _streamRevision + 1,
                    attempt.getStreamRevision(),
                    (commit == null ? List.of(): List.of(commit)));
            clearChanges();
        });
    }

    private CommitAttempt buildCommitAttempt(UUID commitId) {
        return new CommitAttempt(
                _bucketId,
                _streamId,
                _streamRevision + _events.size(),
                commitId,
                _commitSequence + 1,
                Instant.now(Clock.systemUTC()),
                getUncommittedHeaders(),
                _events);
    }

    @Override
    public void clearChanges() {
        _events.clear();
        _unCommittedHeaders.clear();
    }

    @Override
    public void close() throws Exception {
        if (_disposed) {
            return;
        }
        _disposed = true;
    }
}
