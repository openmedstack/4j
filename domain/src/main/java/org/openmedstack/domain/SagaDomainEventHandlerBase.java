package org.openmedstack.domain;

import openmedstack.MessageHeaders;
import openmedstack.events.BaseEvent;
import openmedstack.events.IHandleEvents;
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
        return beforeHandle(domainEvent, headers)
                .thenApplyAsync(
                        e -> _repository
                                .getById(e.getCorrelationId())
                                .thenApplyAsync(s -> new Tuple<Saga, TBaseEvent>(s, e)))
                .thenCompose(
                        t -> {
                            try {
                                while (!t.isDone()) {
                                }
                                Tuple<Saga, TBaseEvent> tuple = t.get();
                                tuple.a.transition(tuple.b);
                                return _repository.save(tuple.a, m -> {})
                                        .thenComposeAsync(x -> afterHandle(tuple.b, headers));
                            } catch (HandlerForDomainEventNotFoundException | InterruptedException | ExecutionException e) {
                                return CompletableFuture.completedFuture(null);
                            }
                        });
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

