package org.openmedstack.eventstore

class ConflictDetector : IDetectConflicts {
    private val _actions: HashMap<Class<*>, HashMap<Class<*>, (Any, Any) -> Boolean>> = HashMap()
    override fun <TUncommitted, TCommitted> register(handler: (TUncommitted, TCommitted) -> Boolean) {
        try {
            val typeVariables = this.javaClass.getDeclaredMethod("register").typeParameters
            val uncommitted = Class.forName(typeVariables[0].typeName)
            val committed = Class.forName(typeVariables[1].typeName)
            if (!_actions.containsKey(uncommitted)) {
                _actions[uncommitted] = HashMap()
            }

            val func: (Any, Any) -> Boolean = { u: Any, c: Any -> handler.invoke(u as TUncommitted, c as TCommitted) }

            _actions[uncommitted]!![committed] = func

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
        } else map!![committed.javaClass]!!.invoke(uncommitted, committed)
    }
}
