package org.openmedstack.domain;

public interface IConstructSagas {
    Saga build(Class type, String id);
}
