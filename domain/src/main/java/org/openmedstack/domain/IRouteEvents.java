package org.openmedstack.domain;

import openmedstack.events.BaseEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IRouteEvents {
    <T extends BaseEvent> void register(Consumer<T> handler);

    CompletableFuture dispatch(Object eventMessage) throws HandlerForDomainEventNotFoundException;
}
//
//public interface IDetectConflicts
//{
//    <TUncommitted, TCommitted> void Register(Function<Pair<TUncommitted, TCommitted>, Boolean> handler)
//    //where TUncommitted : class where TCommitted : class;
//
//    Boolean ConflictsWith(Iterable<Object> uncommittedEvents, Iterable<Object> committedEvents);
//}
