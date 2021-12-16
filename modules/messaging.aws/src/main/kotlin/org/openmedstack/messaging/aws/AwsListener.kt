package org.openmedstack.messaging.aws

import org.openmedstack.DeploymentConfiguration
import org.openmedstack.IProvideTopic
import org.openmedstack.ReflectionTool
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.messaging.CloudEventFactory
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI
import java.util.stream.Collectors

class AwsListener constructor(
    connection: SnsClient,
    private val sqsConnection: SqsClient,
    configuration: DeploymentConfiguration,
    private val _topicProvider: IProvideTopic,
    private val _arnProvider: ArnProvider,
    eventHandlers: Set<IHandleEvents>,
    commandHandlers: Set<IHandleCommands>,
    mapper: CloudEventFactory,
    packages: Set<Package>,
    private val _source: URI
) : AutoCloseable {
    private val _mapper: CloudEventFactory
    private val _configuration: DeploymentConfiguration
    private val _eventQueues: List<Pair<String, PublishHandler>>
    private val _commandQueues: List<Pair<String, SendHandler>>

    init {
        _mapper = mapper
        _configuration = configuration
        val classes: List<Class<*>> = ReflectionTool.findAllClasses(*packages.toTypedArray()).toList()
        _eventQueues = subscribeEventHandlers(connection, sqsConnection, classes, eventHandlers)
        _commandQueues = subscribeCommandHandlers(classes, commandHandlers)
    }

    private fun subscribeEventHandlers(
        client: SnsClient,
        sqsClient: SqsClient,
        classes: List<Class<*>>,
        handlers: Set<IHandleEvents>
    ): List<Pair<String, PublishHandler>> {
        val graph = classes.stream()
            .filter { c -> BaseEvent::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.getTenantSpecific(c) }

        val consumers = ArrayList<Pair<String, PublishHandler>>()
        for (entry in graph) {
            val queue = _arnProvider.getQueue(entry.key) //createQueue(entry)
            val effective: List<IHandleEvents> =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val queueUrl = _arnProvider.getQueue(entry.key)
            client.subscribe { b -> b.endpoint(queueUrl).topicArn(entry.key).protocol("sqs") }

            val handler = PublishHandler(
                { sqsClient.receiveMessage { b -> b.queueUrl(queueUrl) }.messages() },
                _mapper.mapper,
                entry.value.toSet(),
                effective.toSet()
            )
            consumers.add(Pair(queue, handler))
        }
        return consumers
    }

    private fun subscribeCommandHandlers(
        classes: List<Class<*>>,
        handlers: Set<IHandleCommands>
    ): List<Pair<String, SendHandler>> {
        val graph = classes.stream()
            .filter { c -> DomainCommand::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.getTenantSpecific(c) }

        val queues = ArrayList<Pair<String, SendHandler>>()
        for (topic in graph) {
            val queue = _arnProvider.getQueue(topic.key) //createQueue(entry)
            val effective =
                handlers.stream().filter { h -> topic.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val deliverCallback =
                SendHandler(
                    { sqsConnection.receiveMessage { b -> b.queueUrl(queue) }.messages() },
                    _mapper,
                    topic.value.toSet(),
                    effective.toSet(),
                    sqsConnection,
                    _source
                )
            queues.add(Pair(queue, deliverCallback))
        }
        return queues
    }

    override fun close() {
        for (queue in _eventQueues) {
            queue.second.close()
            sqsConnection.deleteQueue { b -> b.queueUrl(queue.first) }
        }
        for (queue in _commandQueues) {
            queue.second.close()
            sqsConnection.deleteQueue { b -> b.queueUrl(queue.first) }
        }
        sqsConnection.close()
    }
}
