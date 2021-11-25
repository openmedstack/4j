package org.openmedstack.eventstore;

public interface Snapshot {
    String getBucketId();

    String getStreamId();

    int getStreamRevision();

    Object getPayload();
}
