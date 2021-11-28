package org.openmedstack.eventstore

import org.openmedstack.Tuple
import java.util.function.Function

interface IDetectConflicts {
    fun <TUncommitted, TCommitted> register(handler: Function<Tuple<TUncommitted, TCommitted>, Boolean>)
    fun conflictsWith(uncommittedEvents: Array<Any>, committedEvents: Array<Any>): Boolean
}