package org.openmedstack.domain

import com.github.oxo42.stateless4j.StateMachine
import com.github.oxo42.stateless4j.triggers.TriggerWithParameters1
import org.openmedstack.events.BaseEvent
import java.util.function.Consumer

class StateMachineRouter<TState, TTrigger>(stateMachine: StateMachine<TState, TTrigger>, getTrigger: (o: BaseEvent) -> TTrigger) : IRouteEvents {
    val _stateMachine: StateMachine<TState, TTrigger>
    val _getTrigger: (o: BaseEvent) -> TTrigger

//    public StateMachineRouter(
//    StateMachine<TState, TTrigger> stateMachine,
//    Func<object, TTrigger> getTrigger,
//    StateMachineTypeCache<TState, TTrigger> typeCache,
//    ILogger<StateMachineRouter<TState, TTrigger>> logger)
//    {
//        this._stateMachine = stateMachine;
//        this._getTrigger = getTrigger;
//        this._typeCache = typeCache;
//        this._logger = (ILogger) logger;
//    }

//    public void Dispose() => GC.SuppressFinalize((object) this);

    @Throws(InvalidTransitionException::class)
    @Suppress("OVERRIDE_BY_INLINE")
    override fun <T : BaseEvent> dispatch(eventMessage: T, type: Class<T>) {
        val trigger: TTrigger = _getTrigger(eventMessage)
        if (!this._stateMachine.canFire(trigger))
            throw InvalidTransitionException("Invalid transition with " + eventMessage.javaClass.name)

        val twp: TriggerWithParameters1<T, TTrigger> = TriggerWithParameters1(trigger, type)
        _stateMachine.fire(twp, eventMessage)
    }

    init {
        _stateMachine = stateMachine
        _getTrigger = getTrigger
    }

    override fun <T : BaseEvent> register(handler: Consumer<T>) {
    }
}