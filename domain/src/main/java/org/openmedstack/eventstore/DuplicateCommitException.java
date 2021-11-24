package org.openmedstack.eventstore;

public class DuplicateCommitException extends Exception {
    public DuplicateCommitException(String message) {
        super(message);
    }
}

