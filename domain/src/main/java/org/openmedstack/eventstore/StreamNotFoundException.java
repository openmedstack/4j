package org.openmedstack.eventstore;

public class StreamNotFoundException extends Exception {
    public StreamNotFoundException(String message) {
        super(message);
    }
}

