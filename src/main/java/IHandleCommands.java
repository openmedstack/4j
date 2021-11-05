import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public interface IHandleCommands<T extends DomainCommand> {
    Future<CommandResponse> handle(T command, MessageHeaders messageHeaders);
}

