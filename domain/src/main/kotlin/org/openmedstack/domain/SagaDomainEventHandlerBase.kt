package org.openmedstack.domain

import org.openmedstack.MessageHeaders
import org.openmedstack.ReflectionTool
import org.openmedstack.Tuple
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.eventstore.SagaRepository
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Consumer
import java.util.function.Function

abstract class SagaDomainEventHandlerBase<TSaga : Saga, TBaseEvent : BaseEvent> protected constructor(private val _repository: SagaRepository) : IHandleEvents {
    override fun handle(domainEvent: BaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        return try {
            //ReflectionTool.getTypeParameter(this, javaClass)
            val type = Class.forName(javaClass.typeParameters[0].typeName) as Class<TSaga>
            beforeHandle(domainEvent as TBaseEvent, headers)
                .thenComposeAsync { e: TBaseEvent ->
                        _repository
                            .getById(type, e!!.correlationId!!)
                            .thenApplyAsync { s: TSaga -> Tuple(s, e) }
                    }
                .thenCompose { tuple: Tuple<TSaga, TBaseEvent> ->
                    try {
                        tuple.a.transition(tuple.b as Any)
                        return@thenCompose _repository.save(tuple.a) { }
                            .thenComposeAsync { afterHandle(tuple.b, headers) }
                    } catch (e: HandlerForDomainEventNotFoundException) {
                        return@thenCompose CompletableFuture.completedFuture<TBaseEvent?>(null)
                    }
                }
        } catch (c: ClassNotFoundException) {
            throw RuntimeException(c)
        }
    }

    protected fun beforeHandle(message: TBaseEvent, headers: MessageHeaders): CompletableFuture<TBaseEvent> {
        return CompletableFuture.completedFuture(message)
    }

    protected fun afterHandle(message: TBaseEvent, headers: MessageHeaders): CompletableFuture<TBaseEvent> {
        return CompletableFuture.completedFuture(message)
    }

    fun close() {}

    override fun <T> canHandle(type: Class<T>): Boolean where T: BaseEvent {
        return try {
            Class.forName(javaClass.typeParameters[1].typeName).isAssignableFrom(type)
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
