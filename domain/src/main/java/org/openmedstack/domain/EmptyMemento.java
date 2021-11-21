package org.openmedstack.domain;

public class EmptyMemento implements Memento {
    private final String _id;
    private final int _version;

    public EmptyMemento(String id, int version) {
        _id = id;
        _version = version;
    }

    @Override
    public String getId() {
        return _id;
    }

    @Override
    public int getVersion() {
        return _version;
    }
}
