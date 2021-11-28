package org.openmedstack.domain

import org.openmedstack.commands.DomainCommand
import org.openmedstack.events.BaseEvent
import java.util.*
import java.util.function.Consumer

abstract class SagaBase protected constructor(id: String, eventRouter: IRouteEvents?) : Saga {
    private val _eventRouter: IRouteEvents
    private val _uncommitted: ArrayList<BaseEvent> = ArrayList()
    private val _undispatched: ArrayList<DomainCommand> = ArrayList()
    private val _id: String
    private var _version = 0

    override val id: String
        get() = _id

    override val version: Int
        get() = _version

    @Throws(HandlerForDomainEventNotFoundException::class)
    override fun transition(message: Any) {
        if (!BaseEvent::class.java.isInstance(message)) {
            return
        }
        _eventRouter.dispatch(message)
        _uncommitted.add(message as BaseEvent)
        _version += 1
    }

    override val uncommittedEvents: Iterable<Any>
        get() =  listOf<Any>(_uncommitted)

    override fun clearUncommittedEvents() {
        _uncommitted.clear()
    }

    override val undispatchedMessages: Iterable<Any>
        get() = listOf<Any>(_undispatched)

    override fun clearUndispatchedMessages() {
        _undispatched.clear()
    }

    protected fun dispatch(message: DomainCommand) {
        _undispatched.add(message)
    }

    protected fun register(handler: Consumer<BaseEvent>) {
        _eventRouter.register(handler)
    }

    init {
        _eventRouter = eventRouter ?: ConventionEventRouter(true, this)
        _id = id
    }
}
