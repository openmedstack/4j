package org.openmedstack.messaging.guice

import com.google.inject.AbstractModule
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.events.IPublishEvents
import org.openmedstack.messaging.InMemoryPublisher
import org.openmedstack.messaging.InMemoryRouter

class InMemoryMessagingModule: AbstractModule() {
    override fun configure() {
        super.configure()

        bind(IRouteCommands::class.java).toConstructor(InMemoryRouter::class.java.getConstructor(Set::class.java))
        bind(IPublishEvents::class.java).toConstructor(InMemoryPublisher::class.java.getConstructor(Set::class.java))
    }
}

