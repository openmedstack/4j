package org.openmedstack.eventstore;

import org.openmedstack.domain.Saga;

public interface IConstructSagas {
    <T extends Saga> T build(Class<T> type, String id);
}
