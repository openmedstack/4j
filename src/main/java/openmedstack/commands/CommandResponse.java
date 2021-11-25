package openmedstack.commands;

import openmedstack.ICorrelate;

public class CommandResponse implements ICorrelate {
    private final String _targetAggregate;
    private final Integer _version;
    private final String _faultMessage;
    private final String _correlationId;

    public CommandResponse(String targetAggregate, Integer version, String faultMessage, String correlationId) {
        _targetAggregate = targetAggregate;
        _version = version;
        _faultMessage = faultMessage;
        _correlationId = correlationId;
    }

    public String getTargetAggregate() {
        return _targetAggregate;
    }

    public Integer getVersion() {
        return _version;
    }

    public String getFaultMessage() {
        return _faultMessage;
    }

    @Override
    public String getCorrelationId() {
        return _correlationId;
    }
}