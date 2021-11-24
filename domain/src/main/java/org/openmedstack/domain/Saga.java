package org.openmedstack.domain;

public interface Saga {
    String getId();

    Integer getVersion();

    void transition(Object message) throws HandlerForDomainEventNotFoundException;

    Iterable<Object> getUncommittedEvents();

    void clearUncommittedEvents();

    Iterable<Object> getUndispatchedMessages();

    void clearUndispatchedMessages();
}

