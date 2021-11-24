package openmedstack.readmodels;

import openmedstack.MessageHeaders;
import openmedstack.events.BaseEvent;

import java.util.concurrent.CompletableFuture;

public interface IUpdateReadModel<T extends BaseEvent> {
    CompletableFuture update(T domainEvent, MessageHeaders headers);
}
