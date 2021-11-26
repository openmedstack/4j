package org.openmedstack.domain;

import org.openmedstack.events.BaseEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ConventionEventRouter implements IRouteEvents, AutoCloseable {
    private final HashMap<Class, Consumer<Object>> _handlers = new HashMap<>();
    private final Boolean _throwOnApplyNotFound;

    public ConventionEventRouter(Boolean throwOnApplyNotFound, Object handlerSource) {
        _throwOnApplyNotFound = throwOnApplyNotFound;
        register(handlerSource);
    }

    @Override
    public <T extends BaseEvent> void register(Consumer<T> handler) {
        try {
            var type = handler.getClass().getTypeParameters()[0];
            var c = Class.forName(type.getTypeName());
            register(c, handler);
        }catch (ClassNotFoundException x){ /*Empty*/}
    }

    public void register(Object handlerSource) throws NullPointerException {
        if (handlerSource == null) {
            throw new NullPointerException();
        }

        var handlers = Arrays.stream(
                        handlerSource.getClass().getDeclaredMethods()
                )
                .filter(m -> m.getName() == "apply")
                .filter(m -> m.getParameterCount() == 1)
                .filter(m -> BaseEvent.class.isAssignableFrom(m.getParameterTypes()[0]))
                .iterator();
        while (handlers.hasNext()) {
            Method handler = handlers.next();
            _handlers.put(handler.getParameterTypes()[0], o -> {
                try {
                    handler.invoke(handlerSource, o);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    /*empty*/
                }
            });
        }
    }

    private <T extends BaseEvent> void register(Class messageType, Consumer<T> handler) {
        if (handler != null) {
            _handlers.put(messageType, o -> handler.accept((T) o));
        }
    }

    @Override
    public CompletableFuture dispatch(Object eventMessage) throws HandlerForDomainEventNotFoundException {
        if (eventMessage == null) {
            throw new NullPointerException();
        }

        if (_handlers.containsKey(eventMessage.getClass())) {
            _handlers.get(eventMessage.getClass()).accept(eventMessage);
        } else {
            if (_throwOnApplyNotFound) {
                throw new HandlerForDomainEventNotFoundException("Aggregate of type '${this}' raised an event of type ${eventMessage::class.java.name} but not handler could be found to handle the message.");
            }
        }

        return CompletableFuture.completedFuture(true);
    }

    public void close() {
        _handlers.clear();
    }
}
