package org.openmedstack.domain

import org.openmedstack.IValidateTokens
import org.openmedstack.MessageHeaders
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.eventstore.DuplicateCommitException
import org.openmedstack.eventstore.Repository
import org.openmedstack.eventstore.Snapshot
import java.util.concurrent.CompletableFuture

abstract class CommandHandlerBase protected constructor(private val _repository: Repository, private val _tokenValidator: IValidateTokens) : IHandleCommands {
    override fun handle(command: DomainCommand, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse> {
        return _tokenValidator.validate(messageHeaders.userToken)
            .thenComposeAsync { userToken: String? -> createResponse(userToken, command, messageHeaders) }
    }

    private fun createResponse(
        userToken: String?,
        command: DomainCommand,
        headers: MessageHeaders?
    ): CompletableFuture<CommandResponse?> {
        return verifyUserToken(userToken).thenComposeAsync { b: Boolean ->
            if (b) handleInternal(command, headers)
            else CompletableFuture.completedFuture(CommandResponse.error(command, "Invalid token"))
        }
    }

    protected fun verifyUserToken(token: String?): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync { true }
    }

    protected abstract fun handleInternal(command: DomainCommand, headers: MessageHeaders?): CompletableFuture<CommandResponse>

    protected operator fun <TAggregate : Aggregate, TMemento : Memento> get(
        type: Class<TAggregate>,
        mementoType: Class<Snapshot<TMemento>>,
        id: String
    ): CompletableFuture<TAggregate> {
        return _repository.getById(type, mementoType, id)
    }

    @Throws(DuplicateCommitException::class)
    protected fun <TAggregate : Aggregate, TMemento : Memento> save(aggregate: TAggregate): CompletableFuture<Boolean> {
        return _repository.save<TAggregate, TMemento>(aggregate) { }
    }
}
