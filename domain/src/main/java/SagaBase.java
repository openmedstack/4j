import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public abstract class SagaBase implements Saga {
    private final IRouteEvents _eventRouter;
    private final List<BaseEvent> _uncommitted = new ArrayList<>();
    private final List<DomainCommand> _undispatched = new ArrayList<>();
    private final String _id;
    private Integer _version;

    protected SagaBase(String id, IRouteEvents eventRouter) {
        _eventRouter = eventRouter;
        _id = id;
    }

    public void Transition(BaseEvent message) {

        _eventRouter.dispatch(message);

        _uncommitted.add(message);
        ++_version;
    }

    @Override
    public Iterable<Object> getUncommittedEvents() {
        return Arrays.asList(_uncommitted.toArray());
    }

    @Override
    public void clearUncommittedEvents() {
        _uncommitted.clear();
    }

    @Override
    public Iterable<Object> getUndispatchedMessages() {
        return Arrays.asList(_undispatched.toArray());
    }

    @Override
    public void clearUndispatchedMessages() {
        _undispatched.clear();
    }

    protected void dispatch(DomainCommand message){
        _undispatched.add(message);
    }

    protected void register(Consumer<BaseEvent> handler){
        _eventRouter.register(handler);
    }
}
