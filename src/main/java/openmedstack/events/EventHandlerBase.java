package openmedstack.events;

import openmedstack.MessageHeaders;

import java.util.concurrent.CompletableFuture;

public abstract class EventHandlerBase<T extends BaseEvent> implements IHandleEvents<T> {

    public Boolean canHandle(Class type) {
        return BaseEvent.class.isAssignableFrom(type);
    }

    public CompletableFuture handle(T evt, MessageHeaders headers) {
        return verifyUserToken(headers.getUserToken()).thenApply(b -> {
            if (b) {
                return handleInternal(evt, headers);
            }else{
                return new CompletableFuture();
            }
        });
    }

    protected CompletableFuture<Boolean> verifyUserToken(String token) {
        return new CompletableFuture<Boolean>().completeAsync(() -> Boolean.TRUE);
    }

    protected abstract CompletableFuture handleInternal(T evt, MessageHeaders headers);
}
