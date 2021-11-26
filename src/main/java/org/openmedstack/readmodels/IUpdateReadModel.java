package org.openmedstack.readmodels;

import org.openmedstack.MessageHeaders;
import org.openmedstack.events.BaseEvent;

import java.util.concurrent.CompletableFuture;

public interface IUpdateReadModel<T extends BaseEvent> {
    CompletableFuture update(T domainEvent, MessageHeaders headers);
}
