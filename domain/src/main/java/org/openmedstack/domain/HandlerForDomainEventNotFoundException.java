package org.openmedstack.domain;

public class HandlerForDomainEventNotFoundException extends Exception {
    public HandlerForDomainEventNotFoundException(String message) {
        super(message);
    }
}
