import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public abstract class CommandHandlerBase<T extends DomainCommand> implements IHandleCommands<T> {
    private final Repository _repository;
    private final IValidateTokens _tokenValidator;

    protected CommandHandlerBase(Repository repository, IValidateTokens tokenValidator) {
        _repository = repository;
        _tokenValidator = tokenValidator;
    }

    public Future<CommandResponse> Handle(T command, MessageHeaders headers) {
        final String userToken = _tokenValidator.validate(headers.getUserToken());
        try {
            if (!verifyUserToken(userToken).get()) {
                return new CompletableFuture<CommandResponse>().completeAsync(() -> new CommandResponse(command.getAggregateId(), command.getVersion(), null, command.getCorrelationId()));
            }
        } catch (Exception e) {
            return new CompletableFuture<CommandResponse>().completeAsync(() -> new CommandResponse(command.getAggregateId(), command.getVersion(), e.getMessage(), command.getCorrelationId()));
        }

        try {
            return handleInternal(command, headers).thenApply(r -> Objects.requireNonNullElseGet(r, () -> new CommandResponse(command.getAggregateId(), command.getVersion(), null, command.getCorrelationId())));
        } catch (Exception ex) {
            return new CompletableFuture<CommandResponse>().completeAsync(() -> new CommandResponse(command.getAggregateId(), command.getVersion(), ex.getMessage(), command.getCorrelationId()));
        }
    }

    protected CompletableFuture<Boolean> verifyUserToken(String token) {
        return new CompletableFuture<Boolean>().completeAsync(() -> true);
    }

    protected abstract CompletableFuture<CommandResponse> handleInternal(T command, MessageHeaders headers);

    protected <TAggregate extends Aggregate> Future<TAggregate> get(String id) {
        return _repository.getById(id);
    }

    protected <TAggregate extends Aggregate> Future<Boolean> save(TAggregate aggregate) {
        return _repository.save(aggregate, h -> {
        });
    }
}
