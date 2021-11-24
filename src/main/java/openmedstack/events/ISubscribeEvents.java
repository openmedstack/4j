package openmedstack.events;

import java.util.concurrent.CompletableFuture;

public interface ISubscribeEvents<T extends BaseEvent> {
    CompletableFuture<AutoCloseable> subscribe(IHandleEvents<T> handle);
}
