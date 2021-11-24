package org.openmedstack.eventstore;

import java.util.concurrent.CompletableFuture;

public interface ICommitEvents {
    CompletableFuture<Iterable<ICommit>> getFrom(String bucketId, String streamId, int minRevision, int maxRevision);

    CompletableFuture<ICommit> commit(CommitAttempt attempt);
}
