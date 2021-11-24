package org.openmedstack.eventstore;

import java.util.concurrent.CompletableFuture;

public interface IAccessSnapshots {
    CompletableFuture<Snapshot> GetSnapshot(String bucketId, String streamId, int maxRevision);

    /// <summary>
    ///     Adds the snapshot provided to the stream indicated.
    /// </summary>
    /// <param name="snapshot">The snapshot to save.</param>
    /// <returns>If the snapshot was added, returns true; otherwise false.</returns>
    /// <exception cref="StorageException" />
    /// <exception cref="StorageUnavailableException" />
    CompletableFuture<Boolean> AddSnapshot(Snapshot snapshot);
}
