package org.openmedstack.domain

import org.openmedstack.MessageHeaders
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.eventstore.SagaRepository
import java.util.concurrent.CompletableFuture

abstract class SagaDomainEventHandlerBase<TSaga : Saga, TBaseEvent : BaseEvent> protected constructor(private val _repository: SagaRepository) :
        IHandleEvents {
    @Suppress("UNCHECKED_CAST")
    override fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        return try {
            val type = Class.forName(javaClass.typeParameters[0].typeName) as Class<TSaga>
            beforeHandle(evt as TBaseEvent, headers)
                    .thenComposeAsync { e: TBaseEvent ->
                        _repository
                                .getById(type, e.correlationId!!)
                                .thenApplyAsync { s: TSaga -> Pair(s, e) }
                    }
                    .thenCompose { tuple: Pair<TSaga, TBaseEvent> ->
                        try {
                            tuple.first.transition(tuple.second as BaseEvent)
                            return@thenCompose _repository.save(tuple.first) { }
                                    .thenComposeAsync { afterHandle(tuple.second, headers) }
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

    override fun canHandle(type: Class<*>): Boolean {
        return try {
            Class.forName(javaClass.typeParameters[1].typeName).isAssignableFrom(type)
        } catch (e: ClassNotFoundException) {
            false
        }
    }
}
