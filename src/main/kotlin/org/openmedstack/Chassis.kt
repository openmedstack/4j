package org.openmedstack

import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.events.BaseEvent
import java.util.concurrent.CompletableFuture
import java.util.function.Function

class Chassis private constructor(val configuration: DeploymentConfiguration) : AutoCloseable {
    private val _metadata: HashMap<String, Any> = HashMap()
    private var _builder: Function<DeploymentConfiguration, Service>? = null
    private var _service: Service? = null
    fun usingCustomBuilder(builder: Function<DeploymentConfiguration, Service>?): Chassis {
        _builder = builder
        return this
    }

    fun start(): CompletableFuture<*>? {
        if (_service != null) {
            return CompletableFuture.completedFuture(true)
        }
        _service = _builder!!.apply(configuration)
        return _service!!.start()
    }

    @Throws(NullPointerException::class)
    fun <T : DomainCommand> send(command: T): CompletableFuture<CommandResponse> {
        if (_service == null) {
            throw NullPointerException("Chassis not started")
        }
        return _service!!.send(command)
    }

    @Throws(NullPointerException::class)
    fun <T : BaseEvent> publish(msg: T): CompletableFuture<*> {
        if (_service == null) {
            throw NullPointerException("Chassis not started")
        }
        return _service!!.publish(msg)
    }

    @Throws(NullPointerException::class)
    fun <T : Any> resolve(type: Class<T>): T {
        if (_service == null) {
            throw NullPointerException("Chassis not started")
        }
        return _service!!.resolve(type)
    }

    fun setBuilder(builder: Function<DeploymentConfiguration, Service>): Chassis {
        _builder = builder
        return this
    }

    @Throws(Exception::class)
    override fun close() {
        if (_service != null) {
            _service!!.close()
        }
        _metadata.clear()
    }

    companion object {
        fun from(manifest: DeploymentConfiguration): Chassis {
            return Chassis(manifest)
        }
    }

}