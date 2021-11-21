package org.openmedstack.domain;

import openmedstack.events.DomainEvent;

import java.util.List;

public interface Aggregate {
    String getId();
    int getVersion();
    void applyEvent(Object event) throws HandlerForDomainEventNotFoundException;
    List<DomainEvent> getUncommittedEvents();
    void clearUncommittedEvents();
    Memento getSnapshot();
}

//public interface IDetectConflicts
//     Boolean ConflictDelegate<TUncommitted extends BaseEvent,TCommitted extends BaseEvent >(
//        TUncommitted uncommitted,
//        TCommittedcommitted)
//        where TUncommitted : class where TCommitted : class;