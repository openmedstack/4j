package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.rabbitmq.client.ConnectionFactory
import org.junit.Test
import org.openmedstack.*
import org.openmedstack.events.DomainEvent
import org.openmedstack.messaging.CloudEventFactory
import java.io.IOException
import java.net.URI
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class RabbitMqListenerTest {
    @Test
    fun canConnectToBroker() {
        val configuration = createConfiguration()
        val connectionFactory = buildConnectionFactory(configuration)
        val tenantProvider = ConfigurationTenantProvider(configuration)
        val topicProvider = EnvironmentTopicProvider(tenantProvider, HashMapTopics())
        val connection = connectionFactory.newConnection()
        val mapper = createMapper(configuration)
        val waitHandle = ManualResetEvent()
        val listener = RabbitMqListener(
            connection,
            configuration,
            topicProvider,
            setOf(TestEventHandler(waitHandle)),
            setOf(),
            mapper,
            setOf(TestEvent::class.java.`package`)
        )

        val publisher = RabbitMqPublisher(connection, topicProvider, mapper)
        for (i in IntRange(1, 100)) {
            val publishTask = publisher.publish(TestEvent("test", 1), HashMap())
        }
        waitHandle.waitOne()

        publisher.close()
        listener.close()
        connection.close()
    }

    private fun createMapper(configuration: DeploymentConfiguration): CloudEventFactory {
        val mapper = ObjectMapper()
        val module = SimpleModule()
        module.addDeserializer(OffsetDateTime::class.java, CustomDeserializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        module.addSerializer(OffsetDateTime::class.java, CustomSerializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
        mapper.registerModule(module)
        return CloudEventFactory(
            EnvironmentTopicProvider(ConfigurationTenantProvider(configuration), HashMapTopics()),
            mapper
        )
    }

    private fun createConfiguration(): DeploymentConfiguration {
        val configuration = DeploymentConfiguration()
        configuration.serviceBus = URI.create("rabbitmq://localhost/")
        configuration.serviceBusUsername = "guest"
        configuration.serviceBusPassword = "guest"
        configuration.tenantId = "test1"
        return configuration
    }

    private fun buildConnectionFactory(configuration: DeploymentConfiguration): ConnectionFactory {
        val connectionFactory = ConnectionFactory()
        connectionFactory.host = configuration.serviceBus!!.host
        connectionFactory.username = configuration.serviceBusUsername
        connectionFactory.password = configuration.serviceBusPassword
        connectionFactory.virtualHost =
            if (configuration.serviceBus!!.path.isNullOrBlank()) "/" else configuration.serviceBus!!.path
        connectionFactory.port = 5672
        connectionFactory.connectionTimeout = configuration.timeout!!.toMillis().toInt()
        connectionFactory.useNio()
        return connectionFactory
    }
}

@Topic("TestEvent")
class TestEvent @JsonCreator constructor(
    @JsonProperty("source") source: String,
    @JsonProperty("version") version: Int
) : DomainEvent(source, version, OffsetDateTime.now())

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
