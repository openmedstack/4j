package org.openmedstack.domain

import com.github.oxo42.stateless4j.StateMachine
import com.github.oxo42.stateless4j.StateMachineConfig
import org.openmedstack.commands.DomainCommand
import org.openmedstack.events.BaseEvent
import kotlin.collections.ArrayList

abstract class StateMachineSagaBase<TState, TTrigger> protected constructor(id: String, initialState: TState)
    : Saga {
    private val _eventRouter: StateMachineRouter<TState, TTrigger>
    private val _uncommitted: ArrayList<BaseEvent> = ArrayList()
    private val _undispatched: ArrayList<DomainCommand> = ArrayList()
    private val _id: String
    private var _version: Int = 0

    init {
        val stateMachineConfig: StateMachineConfig<TState, TTrigger> = StateMachineConfig<TState, TTrigger>()
        this.configureStateMachine(stateMachineConfig)
        this._eventRouter = StateMachineRouter<TState, TTrigger>(StateMachine(initialState, stateMachineConfig), ::getTrigger)
        _id = id
    }

    override val id: String
        get() = _id

    override val version: Int
        get() = _version

    override fun transition(message: BaseEvent) {
        this._eventRouter.dispatch(message, message.javaClass)
        this._uncommitted.add(message)
        ++this._version
    }

    protected abstract fun configureStateMachine(stateMachine: StateMachineConfig<TState, TTrigger>)

    protected abstract fun getTrigger(message: BaseEvent): TTrigger

    override val uncommittedEvents: Iterable<BaseEvent>
        get() = _uncommitted

    override val undispatchedMessages: Iterable<Any>
        get() = _undispatched

    override fun clearUncommittedEvents() {
        _uncommitted.clear()
    }

    override fun clearUndispatchedMessages() {
        _undispatched.clear()
    }

    protected fun dispatch(message: DomainCommand) {
        this._undispatched.add(message)
    }
}