package org.openmedstack.messaging.guice

import org.openmedstack.commands.DomainCommand
import java.time.Instant

class TestCommand(aggregateId: String, version: Int, timeStamp: Instant, correlationId: String? = null) :
    DomainCommand(aggregateId, version, timeStamp, correlationId) {}