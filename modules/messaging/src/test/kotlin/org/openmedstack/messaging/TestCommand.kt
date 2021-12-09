package org.openmedstack.messaging

import org.openmedstack.commands.DomainCommand
import java.time.Clock
import java.time.OffsetDateTime

class TestCommand constructor(aggregateId: String, version: Int) :
    DomainCommand(aggregateId, version, OffsetDateTime.now(Clock.systemUTC()), null) {}
