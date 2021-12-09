package org.openmedstack.sample

import com.google.inject.Guice
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openmedstack.Chassis
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.Service
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.domain.guice.EventStoreModule
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IPublishEvents
import org.openmedstack.messaging.guice.DomainModule
import org.openmedstack.messaging.guice.InMemoryMessagingModule
import java.net.URI
import java.util.concurrent.CompletableFuture

class ChassisTest {
    private var chassis: Chassis? = null

    @Before
    fun beforeTest() {
        val config = DeploymentConfiguration()
        config.serviceBus = URI.create("amqp://localhost")
        chassis = Chassis.from(config)
    }

    @Test
    fun configuration() {
        Assert.assertNotNull(chassis!!.configuration)
    }

    @Test
    fun from() {
        Assert.assertNotNull(chassis)
    }

    @Test
    fun start() {
        chassis!!.withServiceBuilder { _: DeploymentConfiguration, p: Array<Package> -> getService(p) }
        chassis!!.start()
    }

    @Test
    fun send() {
        chassis!!.withServiceBuilder { _: DeploymentConfiguration, p: Array<Package> -> getService(p) }
        chassis!!.start()
        val s = chassis!!.send(TestCommand("test"))
        Assert.assertTrue(s is CompletableFuture<*>)
    }

    @Test
    fun publish() {
    }

    @Test
    fun resolve() {
        chassis!!.withServiceBuilder { _, p -> getService(p) }
        chassis!!.start()
        val s = chassis!!.resolve(String::class.java)
        Assert.assertTrue(s is String)
    }

    @Test
    fun close() {
        try {
            chassis!!.close()
        } catch (e: Exception) {
            Assert.fail(e.message)
        }
    }

    private fun getService(p: Array<Package>): Service {
        return object : Service {
            var injector = Guice.createInjector(
                DomainModule(*p),
                EventStoreModule(),
                InMemoryMessagingModule(),
                TestModule()
            )

            override fun start(): CompletableFuture<*> {
                return CompletableFuture.completedFuture(true)
            }

            override fun <T : DomainCommand> send(command: T): CompletableFuture<CommandResponse> {
                val router = injector.getInstance(IRouteCommands::class.java)
                return router.send(command, HashMap())
            }

            override fun <T : BaseEvent> publish(msg: T): CompletableFuture<*> {
                val publisher = injector.getInstance(IPublishEvents::class.java)
                return publisher.publish(msg, HashMap())
            }

            override fun <T : Any> resolve(type: Class<T>): T {
                return injector.getInstance(type) as T
            }

            @Throws(Exception::class)
            override fun close() {
            }
        }
    }
}
