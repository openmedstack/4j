package org.openmedstack.eventstore

import org.openmedstack.events.BaseEvent

class EventMessage(body: BaseEvent, headers: HashMap<String, Any>) {
    private val _body: BaseEvent
    private val _headers: HashMap<String, Any>
    val headers: HashMap<String, Any>
        get() = _headers

    val body: BaseEvent
        get() = _body

    init {
        _headers = headers
        _body = body
    }
}