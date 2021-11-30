package org.openmedstack.sample

import org.openmedstack.commands.DomainCommand
import java.time.Instant

class TestCommand constructor(aggregateId: String) : DomainCommand(aggregateId, 1, Instant.now(), null)

