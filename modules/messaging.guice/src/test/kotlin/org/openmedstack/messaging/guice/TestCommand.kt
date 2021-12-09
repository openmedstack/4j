package org.openmedstack.messaging.guice

import org.openmedstack.commands.DomainCommand
import java.time.OffsetDateTime

class TestCommand(aggregateId: String, version: Int, timeStamp: OffsetDateTime, correlationId: String? = null) :
    DomainCommand(aggregateId, version, timeStamp, correlationId) {}