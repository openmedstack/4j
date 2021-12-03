package org.openmedstack.sample

import org.openmedstack.Chassis
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.domain.guice.EventStoreModule
import org.openmedstack.messaging.guice.InMemoryMessagingModule

class Program {
    fun main(vararg args: String?) {
        val configuration = DeploymentConfiguration()
        configuration.name = "sample"
        val chassis = Chassis.from(configuration)
            .withServiceBuilder { _,_-> DefaultService(EventStoreModule(), InMemoryMessagingModule()) }
        chassis.start()
    }
}
