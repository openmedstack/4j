package org.openmedstack.domain;

import org.openmedstack.MessageHeaders;
import org.openmedstack.events.BaseEvent;
import org.openmedstack.events.IHandleEvents;
import org.openmedstack.Tuple;
import org.openmedstack.eventstore.SagaRepository;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class SagaDomainEventHandlerBase<TSaga extends Saga, TBaseEvent extends BaseEvent> implements IHandleEvents<TBaseEvent> {
    private final SagaRepository _repository;

    protected SagaDomainEventHandlerBase(SagaRepository sagaRepository) {
        _repository = sagaRepository;
    }

    public CompletableFuture handle(TBaseEvent domainEvent, MessageHeaders headers) {
        try {
            var type = (Class<TSaga>) Class.forName(getClass().getTypeParameters()[0].getTypeName());
            return beforeHandle(domainEvent, headers)
                    .thenComposeAsync(
                            e -> _repository
                                    .getById(type, e.getCorrelationId())
                                    .thenApplyAsync(s -> new Tuple<Saga, TBaseEvent>(s, e)))
                    .thenCompose(
                            tuple -> {
                                try {
                                    tuple.a.transition(tuple.b);
                                    return _repository.save(tuple.a, m -> {
                                            })
                                            .thenComposeAsync(x -> afterHandle(tuple.b, headers));
                                } catch (HandlerForDomainEventNotFoundException e) {
                                    return CompletableFuture.completedFuture(null);
                                }
                            });
        } catch (ClassNotFoundException c) {
            throw new RuntimeException(c);
        }
    }

    protected CompletableFuture<TBaseEvent> beforeHandle(TBaseEvent message, MessageHeaders headers) {
        return CompletableFuture.completedFuture(message);
    }

    protected CompletableFuture<TBaseEvent> afterHandle(TBaseEvent message, MessageHeaders headers) {
        return CompletableFuture.completedFuture(message);
    }

    public void close() {
    }

    public Boolean canHandle(Class type) {
        try {
            return Class.forName(getClass().getTypeParameters()[1].getTypeName()).isAssignableFrom(type);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}

