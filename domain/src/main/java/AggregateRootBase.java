import java.util.ArrayList;
import java.util.List;

public abstract class AggregateRootBase<TMemento extends Memento> implements Aggregate {
    private final String _id;
    private final List<DomainEvent> _uncommittedEvents = new ArrayList<>();
    private final IRouteEvents _registeredRoutes = null;
    private Integer _version;

    protected AggregateRootBase(String id, Memento memento) {
        _id = id;
        internalApplySnapshot(memento);
    }

    public String getId() {
        return _id;
    }

    public int getVersion() {
        return _version;
    }

    public List<DomainEvent> getUncommittedEvents() {
        return _uncommittedEvents;
    }

    public Memento getSnapshot() {
        return createSnapshot(_id, _version);
    }

    protected abstract <TMemento extends Memento> void applySnapshot(TMemento memento);

    protected abstract Memento createSnapshot(String id, Integer version);

    private void internalApplySnapshot(Memento snapshot) {
        if (snapshot != null) {
            _version = snapshot.getVersion();
            applySnapshot(snapshot);
        }
    }

    @Override
    public void applyEvent(Object evt) {
        _registeredRoutes.dispatch(evt);
        _version++;
    }

    @Override
    public void clearUncommittedEvents() {
        _uncommittedEvents.clear();
    }
}
