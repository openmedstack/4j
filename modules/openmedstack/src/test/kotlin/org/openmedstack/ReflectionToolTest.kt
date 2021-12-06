package org.openmedstack

import org.junit.Assert
import org.junit.Test
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.readmodels.IUpdateReadModel

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

