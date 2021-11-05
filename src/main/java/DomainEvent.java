import java.time.Instant;
import java.util.concurrent.Future;

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
