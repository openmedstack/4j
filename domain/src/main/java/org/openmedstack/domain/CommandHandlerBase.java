package org.openmedstack.domain;

import openmedstack.IValidateTokens;
import openmedstack.MessageHeaders;
import openmedstack.commands.CommandResponse;
import openmedstack.commands.DomainCommand;
import openmedstack.commands.IHandleCommands;
import org.openmedstack.eventstore.DuplicateCommitException;
import org.openmedstack.eventstore.Repository;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public abstract class CommandHandlerBase<T extends DomainCommand> implements IHandleCommands<T> {
    private final Repository _repository;
    private final IValidateTokens _tokenValidator;

    protected CommandHandlerBase(Repository repository, IValidateTokens tokenValidator) {
        _repository = repository;
        _tokenValidator = tokenValidator;
    }

    public CompletableFuture<CommandResponse> handle(T command, MessageHeaders headers) {
        return _tokenValidator.validate(headers.getUserToken()).thenComposeAsync(userToken -> createResponse(userToken, command, headers));
    }

    private CompletableFuture<CommandResponse> createResponse(String userToken, T command, MessageHeaders headers) {
        try {
            if (!verifyUserToken(userToken).get()) {
                return CompletableFuture.supplyAsync(() -> new CommandResponse(command.getAggregateId(), command.getVersion(), null, command.getCorrelationId()));
            }
        } catch (Exception e) {
            return CompletableFuture.supplyAsync(() -> new CommandResponse(command.getAggregateId(), command.getVersion(), e.getMessage(), command.getCorrelationId()));
        }

        try {
            return handleInternal(command, headers).thenApply(r -> Objects.requireNonNullElseGet(r, () -> new CommandResponse(command.getAggregateId(), command.getVersion(), null, command.getCorrelationId())));
        } catch (Exception ex) {
            return CompletableFuture.supplyAsync(() -> new CommandResponse(command.getAggregateId(), command.getVersion(), ex.getMessage(), command.getCorrelationId()));
        }
    }

    protected CompletableFuture<Boolean> verifyUserToken(String token) {
        return CompletableFuture.supplyAsync(() -> true);
    }

    protected abstract CompletableFuture<CommandResponse> handleInternal(T command, MessageHeaders headers);

    protected <TAggregate extends Aggregate> CompletableFuture<TAggregate> get(Class<TAggregate> type, String id) {
        return _repository.getById(type, id);
    }

    protected <TAggregate extends Aggregate> CompletableFuture<Boolean> save(TAggregate aggregate) throws DuplicateCommitException {
        return _repository.save(aggregate, h -> { });
    }
}
