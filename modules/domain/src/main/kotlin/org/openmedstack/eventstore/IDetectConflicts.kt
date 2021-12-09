package org.openmedstack.eventstore

interface IDetectConflicts {
    fun <TUncommitted, TCommitted> register(handler: (TUncommitted, TCommitted) -> Boolean)
    fun conflictsWith(uncommittedEvents: Array<Any>, committedEvents: Array<Any>): Boolean
}
