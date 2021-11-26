package org.openmedstack.sample;

import org.openmedstack.commands.DomainCommand;

import java.time.Instant;

public class TestCommand extends DomainCommand {
    protected TestCommand(String aggregateId) {
        super(aggregateId, 1, Instant.now(), null);
    }
}
