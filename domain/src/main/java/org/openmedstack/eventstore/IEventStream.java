package org.openmedstack.eventstore;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IEventStream extends AutoCloseable {
    String BucketId();

    String getStreamId();

    int getStreamRevision();

    int getCommitSequence();

    List<EventMessage> getCommittedEvents();

    HashMap<String, Object> getCommittedHeaders();

    List<EventMessage> getUncommittedEvents();

    HashMap<String, Object> getUncommittedHeaders();

    void add(EventMessage uncommittedEvent);

    CompletableFuture commitChanges(UUID commitId) throws DuplicateCommitException, ConcurrencyException;

    void clearChanges();
}


