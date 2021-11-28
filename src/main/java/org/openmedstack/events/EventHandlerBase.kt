package org.openmedstack.events

import org.openmedstack.MessageHeaders
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.CompletableFuture
import kotlin.reflect.KClass

abstract class EventHandlerBase<T : BaseEvent?> : IHandleEvents<T> {
    final override fun canHandle(type: Class<*>): Boolean {
        return Class.forName(javaClass.typeParameters[0].name).isAssignableFrom(type)
    }

    override fun handle(evt: T, headers: MessageHeaders): CompletableFuture<*> {
        return verifyUserToken(headers.userToken).thenApply { b: Boolean ->
            if (b) {
                return@thenApply handleInternal(evt, headers)
            } else {
                return@thenApply CompletableFuture<Any>()
            }
        }
    }

    protected fun verifyUserToken(token: String?): CompletableFuture<Boolean> {
        return CompletableFuture<Boolean>().completeAsync { java.lang.Boolean.TRUE }
    }

    protected abstract fun handleInternal(evt: T, headers: MessageHeaders?): CompletableFuture<*>?
}
