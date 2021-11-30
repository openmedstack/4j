package org.openmedstack.messaging.inmemory

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.openmedstack.MessageHeaders
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import java.util.concurrent.CompletableFuture

class InMemoryRouterTest : TestCase() {

    public override fun setUp() {
        super.setUp()
    }

    @Test
    fun testSend() {
        var router = InMemoryRouter(setOf(TestCommandHandler() as IHandleCommands))
        var result = router.send(TestCommand("test", 1), HashMap()).get()

        Assert.assertNull(result.faultMessage)
    }
}

class TestCommandHandler : IHandleCommands {
    override fun <T> canHandle(type: Class<T>): Boolean where T: DomainCommand {
        return TestCommand::class.java.isAssignableFrom(type)
    }

    override fun handle(command: DomainCommand, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse> {
        return CompletableFuture.completedFuture(CommandResponse.success(command))
    }
}