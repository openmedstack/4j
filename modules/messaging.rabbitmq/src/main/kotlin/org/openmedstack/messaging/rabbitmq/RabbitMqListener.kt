package org.openmedstack.messaging.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.IProvideTopic
import org.openmedstack.ReflectionTool
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.messaging.CloudEventFactory
import java.util.stream.Collectors

class RabbitMqListener constructor(
    connection: Connection,
    configuration: DeploymentConfiguration,
    private val _topicProvider: IProvideTopic,
    eventHandlers: Set<IHandleEvents>,
    commandHandlers: Set<IHandleCommands>,
    mapper: CloudEventFactory,
    packages: Set<Package>
) : AutoCloseable {
    private val _mapper: CloudEventFactory
    private val _configuration: DeploymentConfiguration
    private val _channel: Channel
    private val _eventQueues: List<String>
    private val _commandQueues: List<String>

    init {
        _channel = connection.createChannel()
        _mapper = mapper
        _configuration = configuration
        val classes: List<Class<*>> = ReflectionTool.findAllClasses(*packages.toTypedArray()).toList()
        _eventQueues = subscribeEventHandlers(classes, eventHandlers)
        _commandQueues = subscribeCommandHandlers(classes, commandHandlers)
    }

    private fun subscribeEventHandlers(classes: List<Class<*>>, handlers: Set<IHandleEvents>): List<String> {
        val graph = classes.stream()
            .filter { c -> BaseEvent::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.getTenantSpecific(c) }

        val consumers = ArrayList<String>()
        for (entry in graph) {
            val queue = createQueue(entry)
            val effective: List<IHandleEvents> =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val deliverCallback = PublishHandler(_mapper.mapper, entry.value.toSet(), effective.toSet())
            _channel.basicConsume(queue.queue, true, deliverCallback) { _: String -> }
            consumers.add(queue.queue)
        }
        return consumers
    }

    private fun subscribeCommandHandlers(classes: List<Class<*>>, handlers: Set<IHandleCommands>): List<String> {
        val graph = classes.stream()
            .filter { c -> DomainCommand::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.getTenantSpecific(c) }

        val queues = ArrayList<String>()
        for (entry in graph) {
            val queue = createQueue(entry)
            val effective =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val deliverCallback =
                SendHandler(_mapper.mapper, entry.value.toSet(), effective.toSet(), _channel, _topicProvider)
            _channel.basicConsume(queue.queue, true, deliverCallback) { _: String -> }
            queues.add(queue.queue)
        }
        return queues
    }

    private fun createQueue(entry: Map.Entry<String, List<Class<*>>>): AMQP.Queue.DeclareOk {
        _channel.exchangeDeclare(entry.key, BuiltinExchangeType.TOPIC)
        val queue = _channel.queueDeclare()
        _channel.queueBind(queue.queue, entry.key, "#")
        return queue
    }

    override fun close() {
        for (queue in _eventQueues) {
            _channel.queueDelete(queue)
        }
        for (queue in _commandQueues) {
            _channel.queueDelete(queue)
        }
        _channel.close()
    }
}
