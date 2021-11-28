package org.openmedstack.sample

import org.openmedstack.Chassis
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.domain.guice.EventStoreModule
import org.openmedstack.messaging.rabbitmq.guice.RabbitMqModule

object Program {
    fun Main(vararg args: String?) {
        val configuration = DeploymentConfiguration()
        configuration.name = "sample"
        val chassis = Chassis.from(configuration)
            .setBuilder { c: DeploymentConfiguration -> DefaultService(EventStoreModule(), RabbitMqModule(c)) }
        chassis.start()
    }
}
