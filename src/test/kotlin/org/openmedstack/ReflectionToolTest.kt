package org.openmedstack

import org.junit.Assert
import org.junit.Test
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.readmodels.IUpdateReadModel
import java.util.concurrent.CompletableFuture
import kotlin.streams.toList

class ReflectionToolTest  {
    @Test
    fun testGetTypeParameter() {
        val updater = TestUpdater()
        var type = ReflectionTool.Companion.getTypeParameter(updater::class.java, IUpdateReadModel::class.java)

        Assert.assertEquals(BaseEvent::class.java, type)
    }

    @Test
    fun canLoadTypes() {
        val classes = ReflectionTool.Companion.findAllClasses(IHandleEvents::class.java.`package`).toList()
        Assert.assertEquals(6, classes.size)
    }
}

class TestUpdater: IUpdateReadModel {
    override fun canUpdate(eventType: Class<*>): Boolean {
        return true
    }
    override fun update(domainEvent: BaseEvent, headers: MessageHeaders?): CompletableFuture<*> {
        return CompletableFuture.completedFuture(true)
    }
}