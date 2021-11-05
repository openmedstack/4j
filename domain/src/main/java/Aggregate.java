import java.util.List;

public interface Aggregate {
    String getId();
    int getVersion();
    void applyEvent(Object event);
    List<DomainEvent> getUncommittedEvents();
    void clearUncommittedEvents();
    Memento getSnapshot();
}
