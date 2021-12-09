package org.openmedstack.messaging.activemq.guice

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Provider
import org.apache.activemq.pool.PooledConnectionFactory
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.ILookupServices
import org.openmedstack.IProvideTopic
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.events.IPublishEvents
import org.openmedstack.messaging.activemq.ActiveMqListener
import org.openmedstack.messaging.activemq.ActiveMqPublisher
import org.openmedstack.messaging.activemq.ActiveMqRouter
import java.io.IOException
import java.lang.reflect.Constructor
import java.net.URISyntaxException
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.concurrent.TimeoutException
import javax.jms.Connection
import javax.jms.ConnectionFactory
import javax.jms.Session

class ActiveMqModule(private val _configuration: DeploymentConfiguration) : AbstractModule() {
    override fun configure() {
        bind(ConnectionFactory::class.java).toProvider(Provider { connectionFactory }).asEagerSingleton()
        bind(Connection::class.java).toProvider(ConnectionProvider::class.java)
        bind(Session::class.java).toProvider(SessionProvider::class.java)
        bind(IPublishEvents::class.java).toConstructor(
            ActiveMqPublisher::class.java.getConstructor(
                Connection::class.java,
                IProvideTopic::class.java,
                ObjectMapper::class.java
            )
        )
        bind(IRouteCommands::class.java).toConstructor(
            ActiveMqRouter::class.java.getConstructor(
                Connection::class.java,
                ILookupServices::class.java,
                IProvideTopic::class.java,
                ObjectMapper::class.java
            )
        )
        bind(ActiveMqListener::class.java).toConstructor(ActiveMqListener::class.java.constructors[0] as Constructor<ActiveMqListener>)
    }

    @get:Throws(NoSuchAlgorithmException::class, KeyManagementException::class, URISyntaxException::class)
    private val connectionFactory: ConnectionFactory
        get() {
            return PooledConnectionFactory(_configuration.serviceBus.toString())
        }
}

internal class ConnectionProvider @Inject constructor(private val _connectionFactory: ConnectionFactory) :
    Provider<Connection> {
    override fun get(): Connection {
        return try {
            _connectionFactory.createConnection()
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: TimeoutException) {
            throw RuntimeException(e)
        }
    }
}

internal class SessionProvider @Inject constructor(private val _connection: Connection) :
    Provider<Session> {
    override fun get(): Session {
        return try {
            _connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        } catch (e: IOException) {
            throw RuntimeException(e)
        } catch (e: TimeoutException) {
            throw RuntimeException(e)
        }
    }
}
