package org.openmedstack

interface IProvideTopic {
    fun <T> getTenantSpecific(type: Class<T>): String
    fun <T> get(type: Class<T>): String
}