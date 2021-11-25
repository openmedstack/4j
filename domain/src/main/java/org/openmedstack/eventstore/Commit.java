package org.openmedstack.eventstore;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface Commit {
    String getBucketId();

    String getStreamId();

    int getStreamRevision();

    UUID getCommitId();

    int getCommitSequence();

    Instant getCommitStamp();

    HashMap<String, Object> getHeaders();

    List<EventMessage> getEvents();

    long getCheckpointToken();
}
