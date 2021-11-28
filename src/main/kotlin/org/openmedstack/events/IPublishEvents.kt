package org.openmedstack.events

import java.util.concurrent.CompletableFuture

interface IPublishEvents {
    fun <T : BaseEvent> publish(evt: T, headers: HashMap<String, Any>?): CompletableFuture<*>
}
