package openmedstack.events;

import openmedstack.*;
import java.time.Instant;

public abstract class BaseEvent implements ICorrelate {
    private final String _source;
    private final Instant _timeStamp;
    private final String _correlationId;

    protected BaseEvent(String source, Instant timeStamp) {
        this(source, timeStamp, null);
    }


    protected BaseEvent(String source, Instant timeStamp, String correlationId) {
        if (timeStamp == Instant.MIN) {
            throw new IllegalArgumentException();
        }

        _source = source;
        _timeStamp = timeStamp;
        _correlationId = correlationId;
    }

    public String getSource(){
        return _source;
    }

    public Instant getTimestamp(){
        return _timeStamp;
    }

    @Override
    public String getCorrelationId(){
        return _correlationId;
    }
}
