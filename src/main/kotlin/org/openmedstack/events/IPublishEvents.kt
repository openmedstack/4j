package org.openmedstack.events

import java.util.concurrent.CompletableFuture

interface IPublishEvents {
    fun <T> publish(evt: T, headers: HashMap<String, Any>): CompletableFuture<*> where T: BaseEvent
}
