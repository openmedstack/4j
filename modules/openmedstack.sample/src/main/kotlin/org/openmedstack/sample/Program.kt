package org.openmedstack.sample

import org.openmedstack.Chassis
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.MessageHeaders
import org.openmedstack.domain.guice.EventStoreModule
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.DomainEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.messaging.guice.DomainModule
import org.openmedstack.messaging.guice.InMemoryMessagingModule
import java.net.URI
import java.time.OffsetDateTime
import java.util.concurrent.CompletableFuture

fun main(args: Array<String>) {
    println("Starting")
    val configuration = DeploymentConfiguration()
    configuration.name = "sample"
    configuration.serviceBus = URI.create("http://localhost")
    configuration.queueName = "sample"
    configuration.tenantId = "t1"
    val chassis = Chassis.from(configuration)
        .definedIn(SampleEventHandler::class.java.`package`)
        .withServiceBuilder { _, p ->
            DefaultService(
                EventStoreModule(),
                InMemoryMessagingModule(),
                DomainModule(*p.toTypedArray())
            )
        }
    chassis.start()
    println("Started")
    chassis.publish(SampleEvent("sample", 0))
    Thread.sleep(1000)
    chassis.close()
    println("Finished")
}

class SampleEvent(source: String, version: Int) : DomainEvent(source, version, OffsetDateTime.now())

class SampleEventHandler : IHandleEvents {
    override fun canHandle(type: Class<*>): Boolean {
        return SampleEvent::class.java.isAssignableFrom(type)
    }

    override fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        return CompletableFuture.runAsync { println("From event handler") }
    }
}
