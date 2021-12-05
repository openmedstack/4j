package org.openmedstack

interface IProvideTopic{
    fun <T> get(type: Class<T>): String
}