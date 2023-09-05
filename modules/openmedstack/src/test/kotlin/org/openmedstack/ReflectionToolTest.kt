package org.openmedstack

import org.junit.Assert
import org.junit.Test
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.readmodels.IUpdateReadModel
import kotlin.streams.toList

class ReflectionToolTest  {
    @Test
    fun testGetTypeParameter() {
        val updater = TestUpdater()
        val type = ReflectionTool.getTypeParameter(updater::class.java, IUpdateReadModel::class.java)

        Assert.assertEquals(BaseEvent::class.java, type)
    }

    @Test
    fun canLoadTypes() {
        val classes = ReflectionTool.findAllClasses(IHandleEvents::class.java.`package`).toList()
        Assert.assertEquals(6, classes.size)
    }
}

