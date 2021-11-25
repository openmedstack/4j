package org.openmedstack.eventstore;

import java.util.concurrent.CompletableFuture;

public interface ICommitEvents {
    CompletableFuture<Iterable<Commit>> getFrom(String bucketId, String streamId, int minRevision, int maxRevision);

    CompletableFuture<Commit> commit(CommitAttempt attempt);
}
