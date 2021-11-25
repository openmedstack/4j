package openmedstack.events;

import openmedstack.MessageHeaders;
import openmedstack.events.BaseEvent;

import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public abstract class DomainEvent extends BaseEvent {
    private final Integer _version;

    protected DomainEvent(String source, Integer version, Instant timeStamp, String correlationId) {
        super(source, timeStamp, correlationId);
        _version = version;
    }

    public Integer getVersion() {
        return _version;
    }
}

