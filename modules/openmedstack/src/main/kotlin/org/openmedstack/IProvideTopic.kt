package org.openmedstack

interface IProvideTopic {
    fun <T> get(type: Class<T>): String
    fun <T> getCanonical(type: Class<T>): String
}