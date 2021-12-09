package org.openmedstack.guice

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.google.inject.AbstractModule
import org.openmedstack.*
import java.io.IOException
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class DefaultsModule constructor(private val deploymentConfiguration: DeploymentConfiguration) : AbstractModule() {
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

    private class CustomDeserializer(private val formatter: DateTimeFormatter) :
        JsonDeserializer<OffsetDateTime?>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(parser: JsonParser, context: DeserializationContext): OffsetDateTime {
            return OffsetDateTime.parse(parser.text, formatter)
        }
    }

    private class CustomSerializer(private val formatter: DateTimeFormatter) : JsonSerializer<OffsetDateTime>() {
        @Throws(IOException::class, JsonProcessingException::class)
        override fun serialize(value: OffsetDateTime, gen: JsonGenerator, provider: SerializerProvider) {
            gen.writeString(value.format(formatter))
        }
    }

}
