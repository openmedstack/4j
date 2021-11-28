package org.openmedstack

interface MessageHeaders : MutableMap<String, Any?> {
    val userToken: String?
}