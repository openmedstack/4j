package org.openmedstack.domain

import org.openmedstack.events.DomainEvent

abstract class AggregateRootBase<TMemento : Memento> protected constructor(id: String, memento: TMemento?) : Aggregate {
    private val _uncommittedEvents: MutableList<DomainEvent> = ArrayList()
    private val _registeredRoutes: IRouteEvents
    private val _id: String
    private var _version: Int = 0

    override val id: String
        get() = _id

    override val version: Int
        get() = _version

    override val uncommittedEvents: MutableList<DomainEvent>
        get() = _uncommittedEvents

    override fun getSnapshot(): Memento {
        return createSnapshot(_id, _version)
    }

    protected abstract fun applySnapshot(memento: TMemento)

    protected abstract fun createSnapshot(id: String?, version: Int?): Memento

    private fun internalApplySnapshot(snapshot: TMemento?) {
        if (snapshot != null) {
            _version = snapshot.version
            applySnapshot(snapshot)
        }
    }

    @Throws(HandlerForDomainEventNotFoundException::class)
    override fun applyEvent(event: DomainEvent) {
        _registeredRoutes.dispatch(event, event.javaClass)
        _version++
    }

    override fun clearUncommittedEvents() {
        _uncommittedEvents.clear()
    }

    init {
        _id = id
        _registeredRoutes = ConventionEventRouter(true, this)
        internalApplySnapshot(memento)
    }
}