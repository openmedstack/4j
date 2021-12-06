package org.openmedstack.commands

import org.openmedstack.ICorrelate

class CommandResponse(val targetAggregate: String, val version: Int, val faultMessage: String?, override val correlationId: String?) : ICorrelate {

    companion object {
        @kotlin.jvm.JvmStatic
        fun success(command: DomainCommand): CommandResponse {
            return CommandResponse(command.aggregateId, command.version, null, command.correlationId)
        }

        @kotlin.jvm.JvmStatic
        fun error(command: DomainCommand, error: String): CommandResponse {
            return CommandResponse(command.aggregateId, command.version, error, command.correlationId)
        }
    }
}
