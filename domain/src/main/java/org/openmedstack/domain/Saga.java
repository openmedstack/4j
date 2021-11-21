package org.openmedstack.domain;

import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public interface Saga {
    String getId();

    Integer getVersion();

    void transition(Object message);

    Iterable<Object> getUncommittedEvents();

    void clearUncommittedEvents();

    Iterable<Object> getUndispatchedMessages();

    void clearUndispatchedMessages();
}

