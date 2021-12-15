package org.openmedstack.messaging.guice

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import org.openmedstack.IProvideTopic
import org.openmedstack.ReflectionTool
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.messaging.CloudEventFactory
import org.openmedstack.readmodels.IUpdateReadModel
import java.lang.reflect.Constructor

class DomainModule constructor(vararg packages: Package) : AbstractModule() {
    private val _packages: Array<out Package>

    override fun configure() {
        super.configure()
        bind(CloudEventFactory::class.java).toConstructor(
            CloudEventFactory::class.java.getConstructor(
                IProvideTopic::class.java,
                ObjectMapper::class.java
            )
        )
        val classes = ReflectionTool.findAllClasses(*_packages).toList()
        val handlerBinder = Multibinder.newSetBinder(binder(), IHandleEvents::class.java)
        for (c in classes.filter { c -> ReflectionTool.isBindableAs(IHandleEvents::class.java, c) }) {
            handlerBinder.addBinding().toConstructor(c.declaredConstructors[0] as Constructor<IHandleEvents>)
        }
        val commandBinder = Multibinder.newSetBinder(binder(), IHandleCommands::class.java)
        for (c in classes.filter { c -> ReflectionTool.isBindableAs(IHandleCommands::class.java, c) }) {
            commandBinder.addBinding().toConstructor(c.declaredConstructors[0] as Constructor<IHandleCommands>)
        }
        val readModelBinder = Multibinder.newSetBinder(binder(), IUpdateReadModel::class.java)
        for (c in classes.filter { c -> ReflectionTool.isBindableAs(IUpdateReadModel::class.java, c) }) {
            readModelBinder.addBinding()
                .toConstructor(c.declaredConstructors[0] as Constructor<IUpdateReadModel<BaseEvent>>)
        }
    }

    init {
        _packages = packages
    }
}
