package org.openmedstack.domain

class EmptyMemento(private val _id: String, private val _version: Int) : Memento {
    override val id: String
        get() = _id

    override val version: Int
        get() = _version
}
