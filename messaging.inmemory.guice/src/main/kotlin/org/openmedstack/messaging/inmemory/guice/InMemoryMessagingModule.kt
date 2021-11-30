package org.openmedstack.messaging.inmemory.guice

import com.google.inject.AbstractModule
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.events.IPublishEvents
import org.openmedstack.messaging.inmemory.InMemoryPublisher
import org.openmedstack.messaging.inmemory.InMemoryRouter
import java.lang.reflect.Constructor

class InMemoryMessagingModule: AbstractModule() {
    @OptIn(ExperimentalStdlibApi::class)
    override fun configure() {
        super.configure()

        bind(IRouteCommands::class.java).toConstructor(InMemoryRouter::class.java.constructors[0] as Constructor<InMemoryRouter>)
        bind(IPublishEvents::class.java).toConstructor(InMemoryPublisher::class.java.constructors[0] as Constructor<InMemoryPublisher>)
    }
}
