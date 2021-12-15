package org.openmedstack.sample

import com.fasterxml.jackson.annotation.JsonProperty
import com.google.inject.Inject
import org.openmedstack.Chassis
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.MessageHeaders
import org.openmedstack.Topic
import org.openmedstack.commands.DomainCommand
import org.openmedstack.domain.guice.EventStoreModule
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.DomainEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.guice.DefaultsModule
import org.openmedstack.messaging.guice.DomainModule
import org.openmedstack.messaging.rabbitmq.guice.RabbitMqModule
import java.net.URI
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

fun main(args: Array<String>) {
    println("Starting")
    val configuration = DeploymentConfiguration()
    configuration.name = "sample"
    configuration.serviceBus = URI.create("amqp://localhost")
    configuration.serviceBusUsername = "guest"
    configuration.serviceBusPassword = "guest"
    configuration.queueName = "sample"
    configuration.tenantId = "t1"
    configuration.services = HashMap()
    configuration.services[Pattern.compile(".+")] = URI("amqp://localhost/t1SampleCommand")

    val chassis = Chassis.from(configuration)
        .definedIn(SampleEvent::class.java.`package`)
        .withServiceBuilder { c, p ->
            DefaultService(
                EventStoreModule(),
//                InMemoryMessagingModule(),
                RabbitMqModule(configuration, *p),
                DomainModule(*p),
                DefaultsModule(c)
            )
        }
    chassis.start()
    println("Started")
    val futures = IntRange(0, 1).map { i ->
        //chassis.send(SampleCommand("cmd$i", i))
        // CompletableFuture.supplyAsync { chassis.send(SampleCommand("cmd$i", i)) }
        chassis.publish(SampleEvent("sample", i))
    }.toTypedArray()
    CompletableFuture.allOf(*futures).join()
    Thread.sleep(3000)
    chassis.close()
    println("Finished")
    Runtime.getRuntime().exit(0)
}

@Topic("Sample")
class SampleEvent @Inject constructor(@JsonProperty("source") source: String, @JsonProperty("version") version: Int) :
    DomainEvent(source, version, OffsetDateTime.now())

@Topic("SampleCommand")
class SampleCommand @Inject constructor(
    @JsonProperty("aggregateId") aggregateId: String,
    @JsonProperty("version") version: Int
) :
    DomainCommand(aggregateId, version, OffsetDateTime.now(), UUID.randomUUID().toString())

class SampleEventHandler : IHandleEvents {
    override fun canHandle(type: Class<*>): Boolean {
        return SampleEvent::class.java.isAssignableFrom(type)
    }

    override fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        println("From event handler")
        return CompletableFuture.completedFuture(true)
    }
}
