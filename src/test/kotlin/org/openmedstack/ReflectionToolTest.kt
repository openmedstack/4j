package org.openmedstack

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import java.util.concurrent.CompletableFuture

class ReflectionToolTest : TestCase() {
    @Test
    fun testGetTypeParameter() {
        val handler = TestHandler()
        var type = ReflectionTool.Companion.getTypeParameter(handler, IHandleCommands::class.java)

        Assert.assertTrue(handler is IHandleCommands)
        Assert.assertEquals(DomainCommand::class.java, type)
    }
}

class TestHandler : IHandleCommands {
    override fun <T> canHandle(type: Class<T>): Boolean where T: DomainCommand {
        return true
    }

    override fun handle(command: DomainCommand, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse> {
        return CompletableFuture.completedFuture(CommandResponse.success(command))
    }
}