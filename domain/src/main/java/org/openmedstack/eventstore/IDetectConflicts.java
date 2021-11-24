package org.openmedstack.eventstore;

import org.openmedstack.Tuple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface IDetectConflicts
{
    <TUncommitted, TCommitted> void register(Function<Tuple<TUncommitted, TCommitted>, Boolean> handler);

    Boolean conflictsWith(Object[] uncommittedEvents, Object[] committedEvents);
}

