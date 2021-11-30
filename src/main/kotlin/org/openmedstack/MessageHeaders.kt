package org.openmedstack

interface MessageHeaders : Map<String, Any> {
    val userToken: String?
}