package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.*
import io.cloudevents.CloudEvent
import io.cloudevents.core.CloudEventUtils.mapData
import io.cloudevents.core.data.PojoCloudEventData
import io.cloudevents.jackson.PojoCloudEventDataMapper
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.IProvideTopic
import org.openmedstack.MessageHeadersImpl
import org.openmedstack.ReflectionTool
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors
import kotlin.streams.toList

class RabbitMqListener constructor(
    connection: Connection,
    configuration: DeploymentConfiguration,
    private val _topicProvider: IProvideTopic,
    private val _handlers: Set<IHandleEvents>,
    mapper: ObjectMapper,
    vararg packages: Package) {
    private val _connection: Connection
    private val _mapper: ObjectMapper
    private val _configuration: DeploymentConfiguration
    private val _channel: Channel
    init {
        _connection = connection
        _mapper = mapper
        _configuration = configuration
        val graph = ReflectionTool.findAllClasses(*packages)
            .filter { c -> BaseEvent::class.java.isAssignableFrom(c) }
            .filter { c -> _handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.get(c) }

        _channel = _connection.createChannel()
        for (entry in graph) {
            _channel.exchangeDeclare(entry.key, BuiltinExchangeType.TOPIC)
            val queue = _channel.queueDeclare()
            _channel.queueBind(queue.queue, entry.key, "#")
            val handlers =
                _handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val deliverCallback = DeliverHandler(_mapper, entry.value.toSet(), handlers.toSet())
            _channel.basicConsume(queue.queue, true, deliverCallback) { _: String -> }
        }
    }
}

class DeliverHandler constructor(private val _mapper: ObjectMapper, private val _types: Set<Class<*>>, private val _handlers: Set<IHandleEvents>): DeliverCallback {

    override fun handle(tag: String?, d: Delivery) {
        val inputEvent = _mapper.readValue(d.body, CloudEvent::class.java)
        for (type in _types) {
            val cloudEventData: PojoCloudEventData<out Any> = mapData(
                inputEvent,
                PojoCloudEventDataMapper.from(_mapper, type)
            )

            val evt = cloudEventData.value
            val futures =
                Arrays.stream(_handlers.toTypedArray()).filter { h -> h.canHandle(evt::class.java) }
                    .map { h -> h.handle(evt as BaseEvent, MessageHeadersImpl(HashMap())) }
                    .toArray { i: Int -> arrayOfNulls<CompletableFuture<*>>(i) }
            CompletableFuture.allOf(*futures).get()
        }
    }
}
