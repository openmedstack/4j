package org.openmedstack.eventstore;

import java.util.concurrent.CompletableFuture;

public interface IStoreEvents extends AutoCloseable
{
    //IPersistStreams advanced ();

    CompletableFuture<IEventStream> createStream(String bucketId, String streamId);

    CompletableFuture<IEventStream> openStream(String bucketId, String streamId, int minRevision, int maxRevision);

    CompletableFuture<IEventStream> openStream(Snapshot snapshot, int maxRevision);
}
