package org.openmedstack.eventstore;

import java.util.concurrent.CompletableFuture;

public interface ITrackCheckpoints {
    CompletableFuture<Long> getLatest();

    CompletableFuture SetLatest(long value);
}
