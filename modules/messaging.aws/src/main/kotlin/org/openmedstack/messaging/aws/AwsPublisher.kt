package org.openmedstack.messaging.aws

import org.openmedstack.IProvideTopic
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IPublishEvents
import org.openmedstack.messaging.CloudEventFactory
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture

class AwsPublisher constructor(
    private val connection: SnsClient,
    private val sqsConnection: SqsClient,
    topicProvider: IProvideTopic,
    private val mapper: CloudEventFactory
) : IPublishEvents {
    private val _source: URI
    private val _arnMap = ArnProvider(connection, sqsConnection, topicProvider)

    override fun <T : BaseEvent> publish(evt: T, headers: HashMap<String, Any>): CompletableFuture<*> {
        return CompletableFuture.runAsync {
            connection.publish { b ->
                b.topicArn(_arnMap.getTopic(evt::class.java)).message(
                    mapper.asString(
                        mapper.toCloudEvent(UUID.randomUUID().toString(), evt, _source)
                    )
                )
            }
        }
    }

    init {
        val tmp = URI.create(connection.toString())
        _source = URI(tmp.scheme, null, tmp.host, tmp.port, tmp.path, null, null)
    }
}
