package org.openmedstack.messaging.inmemory

import org.openmedstack.commands.DomainCommand
import java.time.Instant

class TestCommand constructor(aggregateId: String, version: Int) : DomainCommand(aggregateId, version, Instant.now(), null) {}