package org.openmedstack.eventstore

class EventMessage(body: Any, headers: HashMap<String, Any>) {
    private val _body: Any
    private val _headers: HashMap<String, Any>
    val headers: HashMap<String, Any>
        get() = _headers

    val body: Any
        get() = _body

    init {
        _headers = headers
        _body = body
    }
}