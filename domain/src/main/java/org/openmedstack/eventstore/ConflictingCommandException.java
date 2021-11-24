package org.openmedstack.eventstore;

public class ConflictingCommandException extends Exception {
    public ConflictingCommandException(String message, Throwable innerException) {
        super(message, innerException);
    }
}
