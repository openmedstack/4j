package org.openmedstack.events;

import org.openmedstack.MessageHeaders;

import java.util.concurrent.CompletableFuture;

public interface IHandleEvents<T extends BaseEvent> {
    Boolean canHandle(Class type);

    CompletableFuture handle(T evt, MessageHeaders headers);
}