package org.openmedstack.domain;

import openmedstack.MessageHeaders;
import openmedstack.events.BaseEvent;
import openmedstack.events.IHandleEvents;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public abstract class SagaDomainEventHandlerBase<TSaga extends Saga, TBaseEvent extends BaseEvent> implements IHandleEvents<TBaseEvent> {
    private final SagaRepository _repository;

    protected SagaDomainEventHandlerBase(SagaRepository sagaRepository) {
        _repository = sagaRepository;
    }

    public CompletableFuture handle(TBaseEvent domainEvent, MessageHeaders headers) {
        return beforeHandle(domainEvent, headers)
                .thenApplyAsync(
                        e -> _repository
                                .getById(e.getCorrelationId())
                                .thenApplyAsync(s -> new Tuple<Saga, TBaseEvent>(s, e)))
                .thenApplyAsync(
                        t -> t.thenApplyAsync(tuple -> {
                            tuple.a.transition(tuple.b);
                            return tuple;
                        }))
                .thenApplyAsync(t -> t.thenApply(tuple -> {
                    _repository.save(tuple.a, o -> {
                    });
                    return afterHandle(tuple.b, headers);
                }));
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

