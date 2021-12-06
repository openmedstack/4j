package org.openmedstack.messaging.rabbitmq.guice

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provider
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.ConnectionFactory
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.ILookupServices
import org.openmedstack.IProvideTopic
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.events.IPublishEvents
import org.openmedstack.messaging.rabbitmq.RabbitMqPublisher
import org.openmedstack.messaging.rabbitmq.RabbitMqRouter
import java.io.IOException
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeoutException

class RabbitMqModule(private val _configuration: DeploymentConfiguration) : AbstractModule() {
    override fun configure() {
        bind(IPublishEvents::class.java).toConstructor(RabbitMqPublisher::class.java.getConstructor(Connection::class.java, IProvideTopic::class.java, ObjectMapper::class.java))
        bind(IRouteCommands::class.java).toConstructor(
            RabbitMqRouter::class.java.getConstructor(
                Connection::class.java,
                ILookupServices::class.java,
                IProvideTopic::class.java,
                ObjectMapper::class.java
            )
        )
        bind(ConnectionFactory::class.java).toProvider(Provider { connectionFactory }).asEagerSingleton()
        bind(Connection::class.java).toProvider(ConnectionProvider::class.java)
        bind(Channel::class.java).toProvider(ChannelProvider::class.java)
    }

    @get:Throws(NoSuchAlgorithmException::class, KeyManagementException::class, URISyntaxException::class)
    private val connectionFactory: ConnectionFactory
        get() {
            val conn = ConnectionFactory()
            conn.setUri(_configuration.serviceBus)
            return conn
        }
}

internal class ConnectionProvider @Inject constructor(private val _connectionFactory: ConnectionFactory) : Provider<Connection> {
    override fun get(): Connection {
        return try {
            _connectionFactory.newConnection()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: TimeoutException) {
            throw RuntimeException(e)
        }
    }
}

internal class ChannelProvider @Inject constructor(private val _connectionFactory: ConnectionFactory) : Provider<Channel> {
    override fun get(): Channel {
        return try {
            _connectionFactory.newConnection().createChannel()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: TimeoutException) {
            throw RuntimeException(e)
        }
    }
}
