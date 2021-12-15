package org.openmedstack.messaging.activemq

import org.openmedstack.DeploymentConfiguration
import org.openmedstack.IProvideTopic
import org.openmedstack.ReflectionTool
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.messaging.CloudEventFactory
import java.util.stream.Collectors
import javax.jms.Connection
import javax.jms.Session
import javax.jms.Topic

class ActiveMqListener constructor(
    connection: Connection,
    configuration: DeploymentConfiguration,
    private val _topicProvider: IProvideTopic,
    eventHandlers: Set<IHandleEvents>,
    commandHandlers: Set<IHandleCommands>,
    mapper: CloudEventFactory,
    vararg packages: Package
) : AutoCloseable {
    private val _mapper: CloudEventFactory
    private val _configuration: DeploymentConfiguration
    private val _channel: Session

    init {
        _channel = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        _mapper = mapper
        _configuration = configuration
        val classes = ReflectionTool.findAllClasses(*packages).toList()
        subscribeEventHandlers(classes, eventHandlers)
        subscribeCommandHandlers(classes, commandHandlers)
    }

    private fun subscribeEventHandlers(classes: List<Class<*>>, handlers: Set<IHandleEvents>) {
        val graph = classes.stream()
            .filter { c -> BaseEvent::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.get(c) }

        for (entry in graph) {
            val topic = createTopic(entry)
            val effective: List<IHandleEvents> =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val consumer = _channel.createConsumer(topic)
            consumer.messageListener = PublishHandler(_mapper.mapper, entry.value.toSet(), effective.toSet())
        }
    }

    private fun subscribeCommandHandlers(classes: List<Class<*>>, handlers: Set<IHandleCommands>) {
        val graph = classes.stream()
            .filter { c -> DomainCommand::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.get(c) }

        for (entry in graph) {
            val queue = createTopic(entry)
            val effective =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val consumer = _channel.createConsumer(queue)
            consumer.messageListener = SendHandler(_mapper, entry.value.toSet(), effective.toSet(), _channel)
        }
    }

    private fun createTopic(entry: Map.Entry<String, List<Class<*>>>): Topic {
        return _channel.createTopic(entry.key)
    }

    override fun close() {
        _channel.close()
    }
}
