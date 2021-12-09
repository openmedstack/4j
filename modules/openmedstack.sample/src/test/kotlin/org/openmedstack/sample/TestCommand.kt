package org.openmedstack.sample

import org.openmedstack.commands.DomainCommand
import java.time.OffsetDateTime

class TestCommand constructor(aggregateId: String) : DomainCommand(aggregateId, 1, OffsetDateTime.now(), null)
