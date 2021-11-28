package org.openmedstack.messaging.rabbitmq

import org.openmedstack.commands.CommandResponse.Companion.success
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.commands.DomainCommand
import java.util.HashMap
import java.util.concurrent.CompletableFuture
import org.openmedstack.commands.CommandResponse
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import java.io.IOException
import java.lang.RuntimeException
import java.nio.charset.StandardCharsets

class RabbitMqRouter(private val _connection: Channel) : IRouteCommands {
    override fun <T : DomainCommand> send(
        command: T,
        headers: HashMap<String, Any>?
    ): CompletableFuture<CommandResponse> {
        return try {
            _connection.basicPublish("", "", true, AMQP.BasicProperties(), "".toByteArray(StandardCharsets.UTF_8))
            CompletableFuture.completedFuture(success(command))
        } catch (io: IOException) {
            throw RuntimeException(io)
        }
    }
}
