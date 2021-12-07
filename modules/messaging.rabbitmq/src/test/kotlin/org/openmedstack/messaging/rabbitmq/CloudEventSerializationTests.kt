package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import io.cloudevents.CloudEvent
import org.junit.Test

class CloudEventSerializationTests {
    @Test
    fun canSerialize() {

    }

    @Test
    fun canDeserialize() {
        val json =
            "{\"specversion\":\"1.0\",\"id\":\"4027447e-f41a-46f2-a494-f6cd8a3f9251\",\"source\":\"http://localhost\",\"type\":\"application/cloudevents+json\",\"datacontenttype\":\"application/json+TestEvent\",\"subject\":\"TestEvent\",\"time\":\"2021-12-07T10:43:05.5757149Z\",\"data\":{\"source\":\"test\",\"timestamp\":{\"nano\":562784900,\"epochSecond\":1638873785},\"correlationId\":null,\"version\":1}}"
        val serializer = ObjectMapper()
        val module = SimpleModule()
        module.addDeserializer(CloudEvent::class.java, CloudEventDeserializer())
        serializer.registerModule(module)

        val value = serializer.readValue(json, CloudEvent::class.java)
    }
}
