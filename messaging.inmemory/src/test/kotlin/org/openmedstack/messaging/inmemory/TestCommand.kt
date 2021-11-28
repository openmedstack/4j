package org.openmedstack.messaging.inmemory

class TestCommand constructor(aggregateId: String, version: Int) :
    DomainCommand(aggregateId, version, Instant.now(), null) {}