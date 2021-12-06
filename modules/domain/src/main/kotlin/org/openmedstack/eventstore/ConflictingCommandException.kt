package org.openmedstack.eventstore

class ConflictingCommandException(message: String?, innerException: Throwable?) : Exception(message, innerException)
