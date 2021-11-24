package org.openmedstack.eventstore;

import org.openmedstack.Tuple;

import java.util.HashMap;
import java.util.function.Function;

public class ConflictDetector implements IDetectConflicts {
    private final HashMap<Class, HashMap<Class, Function<Tuple<Object, Object>, Boolean>>> _actions = new HashMap<>();

    public <TUncommitted, TCommitted> void register(Function<Tuple<TUncommitted, TCommitted>, Boolean> handler) {
        try {
            var typeVariables = this.getClass().getDeclaredMethod("register").getTypeParameters();
            var uncommitted = Class.forName(typeVariables[0].getTypeName());
            var committed = Class.forName(typeVariables[1].getTypeName());
            if (!_actions.containsKey(uncommitted)) {
                _actions.put(uncommitted, new HashMap<>());
            }
            _actions.get(uncommitted).put(committed, (Tuple<Object, Object> t) -> handler.apply((Tuple<TUncommitted, TCommitted>) t));
        } catch (NoSuchMethodException | ClassNotFoundException n) {
        }
    }

    public Boolean conflictsWith(Object[] uncommittedEvents, Object[] committedEvents) {
        for (var uncommittedEvent : uncommittedEvents) {
            for (var committedEvent : committedEvents) {
                if (conflicts(uncommittedEvent, committedEvent)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Boolean conflicts(Object uncommitted, Object committed) {
        if (!_actions.containsKey(uncommitted.getClass()))
        //.TryGetValue(uncommitted.GetType(), out var dictionary))
        {
            return uncommitted.getClass() == committed.getClass();
        }

        var map = _actions.get(uncommitted.getClass());
        if (!map.containsKey(committed.getClass())) {
            return true;
        }

        return map.get(committed.getClass()).apply(new Tuple<>(uncommitted, committed));
    }
}
