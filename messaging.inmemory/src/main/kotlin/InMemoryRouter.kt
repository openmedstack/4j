import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IRouteCommands
import java.util.concurrent.CompletableFuture

class InMemoryRouter : IRouteCommands {
    override fun <T : DomainCommand> send(
        command: T,
        headers: HashMap<String, Any>?
    ): CompletableFuture<CommandResponse> {
        return CompletableFuture.completedFuture(CommandResponse.success(command))
    }
}
