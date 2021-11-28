package org.openmedstack.eventstore

import org.openmedstack.Tuple
import java.util.function.Function

class ConflictDetector : IDetectConflicts {
    private val _actions: HashMap<Class<*>, HashMap<Class<*>, Function<Tuple<Any, Any>, Boolean>>> = HashMap()
    override fun <TUncommitted, TCommitted> register(handler: Function<Tuple<TUncommitted, TCommitted>, Boolean>) {
        try {
            val typeVariables = this.javaClass.getDeclaredMethod("register").typeParameters
            val uncommitted = Class.forName(typeVariables[0].typeName)
            val committed = Class.forName(typeVariables[1].typeName)
            if (!_actions.containsKey(uncommitted)) {
                _actions[uncommitted] = HashMap()
            }
            _actions[uncommitted]!![committed] =
                Function { t: Tuple<Any, Any> -> with(handler) { apply(Tuple(t.a as TUncommitted, t.b as TCommitted)) } }
        } catch (n: NoSuchMethodException) {
        } catch (n: ClassNotFoundException) {
        }
    }

    override fun conflictsWith(uncommittedEvents: Array<Any>, committedEvents: Array<Any>): Boolean {
        for (uncommittedEvent in uncommittedEvents) {
            for (committedEvent in committedEvents) {
                if (conflicts(uncommittedEvent, committedEvent)) {
                    return true
                }
            }
        }
        return false
    }

    private fun conflicts(uncommitted: Any, committed: Any): Boolean {
        if (!_actions.containsKey(uncommitted.javaClass)) //.TryGetValue(uncommitted.GetType(), out var dictionary))
        {
            return uncommitted.javaClass == committed.javaClass
        }
        val map = _actions[uncommitted.javaClass]
        return if (!map!!.containsKey(committed.javaClass)) {
            true
        } else map!![committed.javaClass]!!.apply(Tuple(uncommitted, committed))
    }
}