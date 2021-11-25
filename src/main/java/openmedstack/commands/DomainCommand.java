package openmedstack.commands;

import openmedstack.ICorrelate;

import java.time.Instant;

public abstract class DomainCommand implements ICorrelate {
    private final Instant _timestamp;
    private final String _aggregateId;
    private final Integer _version;
    private final String _correlationId;

    protected DomainCommand(String aggregateId, Integer version, Instant timeStamp, String correlationId) {
        if (timeStamp == Instant.MIN) {
            throw new IllegalArgumentException("Cannot use min time");
        }
        _aggregateId = aggregateId;
        _version = version;
        _timestamp = timeStamp;
        _correlationId = correlationId;
    }

    public String getAggregateId() {
        return _aggregateId;
    }

    public Integer getVersion() {
        return _version;
    }

    public Instant getTimestamp() {
        return _timestamp;
    }

    public String getCorrelationId() {
        return _correlationId;
    }
}

