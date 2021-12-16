package org.openmedstack.messaging.aws

import org.openmedstack.ILookupServices
import org.openmedstack.ManualResetEvent
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.messaging.CloudEventFactory
import org.openmedstack.messaging.CloudEventHeaders
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture

class AwsRouter(
    private val connection: SqsClient,
    private val serviceLookup: ILookupServices,
    private val mapper: CloudEventFactory
) : IRouteCommands, AutoCloseable {
    private val _waitBuffer: HashMap<String, ManualResetEvent> = HashMap()
    private val _responseBuffer: HashMap<String, CommandResponse> = HashMap()
    private val _queueUrl: String
    private val _source: URI

    override fun <T> send(
        command: T,
        headers: HashMap<String, Any>
    ): CompletableFuture<CommandResponse> where T : DomainCommand {
        if (command.correlationId.isNullOrBlank()) {
            CompletableFuture.failedFuture<CommandResponse>(RuntimeException("Missing correlation id"))
        }

        return serviceLookup.lookup(command::class.java).thenComposeAsync { uri ->
            if (uri == null) {
                CompletableFuture.failedFuture<CommandResponse>(RuntimeException("No service endpoint found"))
            }

            val messageId = UUID.randomUUID().toString()
            val waitHandle = ManualResetEvent(false)
            _waitBuffer[messageId] = waitHandle
            val msg = mapper.asString(mapper.toCloudEvent(messageId, command, _source))
            val attributes = HashMap<String, MessageAttributeValue>()
            attributes[CloudEventHeaders.responseAddress] =
                MessageAttributeValue.builder().stringValue(_queueUrl).build()

            connection.sendMessage { r ->
                r.messageBody(msg).queueUrl(uri!!.toASCIIString()).messageAttributes(attributes)
            }
            waitHandle.waitOne()
            val result = _responseBuffer.remove(messageId)
            if (result != null) {
                CompletableFuture.completedFuture(result)
            } else {
                CompletableFuture.failedFuture(RuntimeException("Could not find result"))
            }
        }
    }

    override fun close() {
        connection.deleteQueue { r -> r.queueUrl(_queueUrl) }
    }

    init {
        val tmp = URI.create(connection.toString())
        _source = URI(tmp.scheme, null, tmp.host, tmp.port, tmp.path, null, null)
        val response = connection.createQueue { b -> b.queueName(UUID.randomUUID().toString()) }
        _queueUrl = response.queueUrl()
    }
}
