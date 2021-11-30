package org.openmedstack.sample

import org.openmedstack.Chassis
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.domain.guice.EventStoreModule
import org.openmedstack.messaging.inmemory.guice.InMemoryMessagingModule

class Program {
    fun main(vararg args: String?) {
        val configuration = DeploymentConfiguration()
        configuration.name = "sample"
        val chassis = Chassis.from(configuration)
            .withBuilder { DefaultService(EventStoreModule(), InMemoryMessagingModule()) }
        chassis.start()
    }
}
