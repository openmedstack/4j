package org.openmedstack.sample

import org.openmedstack.Chassis
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.domain.guice.EventStoreModule
import org.openmedstack.messaging.guice.DomainModule
import org.openmedstack.messaging.guice.InMemoryMessagingModule
import java.net.URI

fun main(args: Array<String>) {
    println("Starting")
    val configuration = DeploymentConfiguration()
    configuration.name = "sample"
    configuration.serviceBus = URI.create("http://localhost")
    val chassis = Chassis.from(configuration)
        .withServiceBuilder { _, _ -> DefaultService(EventStoreModule(), InMemoryMessagingModule(), DomainModule()) }
    chassis.start()
    println("Started")
    chassis.close()
    println("Finished")
}
