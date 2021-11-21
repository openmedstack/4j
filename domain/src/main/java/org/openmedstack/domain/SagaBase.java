package org.openmedstack.domain;

import openmedstack.commands.DomainCommand;
import openmedstack.events.BaseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

public abstract class SagaBase implements Saga {
    private final IRouteEvents _eventRouter;
    private final ArrayList<BaseEvent> _uncommitted =new ArrayList<BaseEvent>();
    private final ArrayList<DomainCommand> _undispatched =new ArrayList<DomainCommand>();
    private final String _id;
    private int _version = 0;

    protected SagaBase(String id,  IRouteEvents eventRouter) {
        _eventRouter = eventRouter == null ? new ConventionEventRouter(true, this):eventRouter;
        _id = id;
    }

    public void transition(BaseEvent message) throws HandlerForDomainEventNotFoundException {

        _eventRouter.dispatch(message);

        _uncommitted.add(message);
        _version += 1;
    }

    public Iterable<Object> getUncommittedEvents() {
       return Arrays.asList(_uncommitted);
    }

    public void clearUncommittedEvents() {
        _uncommitted.clear();
    }

    public Iterable<Object> getUndispatchedMessages() {
       return Arrays.asList(_undispatched);
    }

    public void clearUndispatchedMessages() {
        _undispatched.clear();
    }

    protected void dispatch( DomainCommand message) {
        _undispatched.add(message);
    }

    protected void register( Consumer<BaseEvent> handler) {
        _eventRouter.register(handler);
    }
}
