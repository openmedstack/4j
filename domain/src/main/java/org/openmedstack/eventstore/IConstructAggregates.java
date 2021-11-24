package org.openmedstack.eventstore;

import org.openmedstack.domain.Aggregate;
import org.openmedstack.domain.Memento;

public interface IConstructAggregates {
    <T extends Aggregate> T build(Class<T> type, String id, Memento snapshot);
}
