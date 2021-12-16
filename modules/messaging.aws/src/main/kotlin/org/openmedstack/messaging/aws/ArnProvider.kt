package org.openmedstack.messaging.aws

import org.openmedstack.IProvideTopic
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.CreateTopicRequest
import software.amazon.awssdk.services.sqs.SqsClient
import java.util.*

class ArnProvider constructor(
    private val snsClient: SnsClient,
    private val sqsClient: SqsClient,
    private val topicProvider: IProvideTopic
) : AutoCloseable {
    private val _arnMap = HashMap<Class<*>, String>()
    private val _topicMap = HashMap<String, String>()

    fun getTopic(type: Class<*>): String {
        if (_arnMap.containsKey(type)) {
            return _arnMap[type]!!
        }
        val tenantSpecificTopic = topicProvider.getTenantSpecific(type)
        val request = CreateTopicRequest.builder().name(tenantSpecificTopic).build()
        val topic = snsClient.createTopic(request)
        _arnMap[type] = topic.topicArn()
        return topic.topicArn()
    }

    fun getQueue(topic: String): String {
        if (_topicMap.containsKey(topic)) {
            return _topicMap[topic]!!
        }

        val queue = sqsClient.createQueue { b -> b.queueName(UUID.randomUUID().toString()) }
        _topicMap[topic] = queue.queueUrl()
        return queue.queueUrl()
    }

    override fun close() {
        for (queue in _topicMap.values) {
            sqsClient.deleteQueue { b -> b.queueUrl(queue) }
        }
    }
}