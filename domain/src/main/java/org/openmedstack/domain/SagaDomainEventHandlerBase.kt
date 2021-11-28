package org.openmedstack.domain

import org.openmedstack.MessageHeaders
import org.openmedstack.Tuple
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.eventstore.SagaRepository
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.function.Consumer
import java.util.function.Function

abstract class SagaDomainEventHandlerBase<TSaga : Saga, TBaseEvent : BaseEvent?> protected constructor(private val _repository: SagaRepository) : IHandleEvents<TBaseEvent> {
    override fun handle(domainEvent: TBaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        return try {
            val type = Class.forName(javaClass.typeParameters[0].typeName) as Class<TSaga>
            beforeHandle(domainEvent, headers)
                .thenComposeAsync(
                    Function<TBaseEvent, CompletionStage<Tuple<Saga, TBaseEvent>>> { e: TBaseEvent ->
                        _repository
                            .getById(type, e!!.correlationId!!)
                            .thenApplyAsync { s: TSaga -> Tuple(s, e) }
                    })
                .thenCompose { tuple: Tuple<Saga, TBaseEvent> ->
                    try {
                        tuple.a.transition(tuple.b as Any)
                        return@thenCompose _repository.save(tuple.a, Consumer { })
                            .thenComposeAsync { afterHandle(tuple.b, headers) }
                    } catch (e: HandlerForDomainEventNotFoundException) {
                        return@thenCompose CompletableFuture.completedFuture<TBaseEvent?>(null)
                    }
                }
        } catch (c: ClassNotFoundException) {
            throw RuntimeException(c)
        }
    }

    protected fun beforeHandle(message: TBaseEvent?, headers: MessageHeaders): CompletableFuture<TBaseEvent> {
        return CompletableFuture.completedFuture(message)
    }

    protected fun afterHandle(message: TBaseEvent?, headers: MessageHeaders): CompletableFuture<TBaseEvent> {
        return CompletableFuture.completedFuture(message)
    }

    fun close() {}
    override fun canHandle(type: Class<*>): Boolean {
        return try {
            Class.forName(javaClass.typeParameters[1].typeName).isAssignableFrom(type)
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
