package org.openmedstack.sample

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.google.inject.AbstractModule
import com.google.inject.Inject
import org.openmedstack.*
import org.openmedstack.domain.guice.EventStoreModule
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.DomainEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.messaging.guice.DomainModule
import org.openmedstack.messaging.rabbitmq.guice.RabbitMqModule
import java.io.IOException
import java.net.URI
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

fun main(args: Array<String>) {
    println("Starting")
    val configuration = DeploymentConfiguration()
    configuration.name = "sample"
    configuration.serviceBus = URI.create("amqp://localhost")
    configuration.serviceBusUsername = "guest"
    configuration.serviceBusPassword = "guest"
    configuration.queueName = "sample"
    configuration.tenantId = "t1"

    val chassis = Chassis.from(configuration)
        .definedIn(SampleEventHandler::class.java.`package`)
        .withServiceBuilder { c, p ->
            DefaultService(
                EventStoreModule(),
//                InMemoryMessagingModule(),
                RabbitMqModule(configuration, *p),
                DomainModule(*p),
                BootstrapModule(c)
            )
        }
    chassis.start()
    println("Started")
    chassis.publish(SampleEvent("sample", 0)).join()
    Thread.sleep(1000)
    chassis.close()
    println("Finished")
    Runtime.getRuntime().exit(0)
}

class SampleEvent @Inject constructor(@JsonProperty("source") source: String, @JsonProperty("version") version: Int) :
    DomainEvent(source, version, OffsetDateTime.now())

class SampleEventHandler : IHandleEvents {
    override fun canHandle(type: Class<*>): Boolean {
        return SampleEvent::class.java.isAssignableFrom(type)
    }

    override fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        println("From event handler")
        return CompletableFuture.completedFuture(true)
    }
}

class BootstrapModule constructor(private val deploymentConfiguration: DeploymentConfiguration) : AbstractModule() {
    override fun configure() {
        bind(IMapTopics::class.java).toInstance(HashMapTopics())
        bind(IProvideTopic::class.java).toConstructor(
            EnvironmentTopicProvider::class.java.getConstructor(
                IProvideTenant::class.java,
                IMapTopics::class.java
            )
        )
        bind(IProvideTenant::class.java).toConstructor(
            ConfigurationTenantProvider::class.java.getConstructor(
                DeploymentConfiguration::class.java
            )
        )
        bind(ILookupServices::class.java).toConstructor(
            FixedServicesLookup::class.java.getConstructor(
                DeploymentConfiguration::class.java
            )
        )
        bind(DeploymentConfiguration::class.java).toInstance(deploymentConfiguration)
        bind(ObjectMapper::class.java).toInstance(createMapper())
    }


    private fun createMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        val module = SimpleModule()
        module.addDeserializer(OffsetDateTime::class.java, CustomDeserializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        module.addSerializer(OffsetDateTime::class.java, CustomSerializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        mapper.registerModule(module)
        return mapper
    }

    class CustomDeserializer(private val formatter: DateTimeFormatter) :
        JsonDeserializer<OffsetDateTime?>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(parser: JsonParser, context: DeserializationContext): OffsetDateTime {
            return OffsetDateTime.parse(parser.text, formatter)
        }
    }

    class CustomSerializer(private val formatter: DateTimeFormatter) : JsonSerializer<OffsetDateTime>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: OffsetDateTime, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value.format(formatter))
        }
    }

}
