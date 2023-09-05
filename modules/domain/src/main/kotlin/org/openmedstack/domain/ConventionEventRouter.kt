package org.openmedstack.domain

import org.openmedstack.events.BaseEvent
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.function.Consumer

class ConventionEventRouter(private val _throwOnApplyNotFound: Boolean, handlerSource: Any?) : IRouteEvents, AutoCloseable {
    private val _handlers: HashMap<Class<*>, Consumer<Any>> = HashMap()

    override fun <T : BaseEvent> register(handler: Consumer<T>) {
        try {
            val c = Class.forName(handler.javaClass.typeParameters[0].typeName)
            register(c, handler)
        } catch (x: ClassNotFoundException) { /*Empty*/
        }
    }

    @Throws(NullPointerException::class)
    fun register(handlerSource: Any?) {
        if (handlerSource == null) {
            throw NullPointerException()
        }
        val handlers = Arrays.stream(
                handlerSource.javaClass.declaredMethods
        )
                .filter { m: Method -> m.name === "apply" }
                .filter { m: Method -> m.parameterCount == 1 }
                .filter { m: Method -> BaseEvent::class.java.isAssignableFrom(m.parameterTypes[0]) }
                .iterator()
        while (handlers.hasNext()) {
            val handler = handlers.next()
            _handlers[handler.parameterTypes[0]] = Consumer { o: Any? ->
                try {
                    handler.invoke(handlerSource, o)
                } catch (e: IllegalAccessException) {
                    /*empty*/
                } catch (e: InvocationTargetException) {
                    /*empty*/
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : BaseEvent?> register(messageType: Class<*>, handler: Consumer<T>) {
        _handlers[messageType] = Consumer { o: Any ->
            run {
                val arg = o as T
                if (arg != null) {
                    handler.accept(arg)
                }
            }
        }
    }

    @Throws(HandlerForDomainEventNotFoundException::class)
    override fun <T : BaseEvent> dispatch(eventMessage: T, type: Class<T>) {
        if (_handlers.containsKey(eventMessage.javaClass)) {
            _handlers[eventMessage.javaClass]!!.accept(eventMessage)
        } else {
            if (_throwOnApplyNotFound) {
                throw HandlerForDomainEventNotFoundException("Aggregate of type '\${this}' raised an event of type \${eventMessage::class.java.name} but not handler could be found to handle the message.")
            }
        }
    }

    override fun close() {
        _handlers.clear()
    }

    init {
        register(handlerSource)
    }
}

