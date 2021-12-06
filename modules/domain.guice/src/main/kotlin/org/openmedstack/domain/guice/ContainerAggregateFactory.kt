package org.openmedstack.domain.guice

import com.google.inject.Inject
import com.google.inject.Injector
import org.openmedstack.domain.Aggregate
import org.openmedstack.domain.Memento
import org.openmedstack.eventstore.IConstructAggregates
import java.lang.reflect.InvocationTargetException
import java.util.*

internal class ContainerAggregateFactory @Inject constructor(private val _container: Injector) : IConstructAggregates {
    override inline fun <T : Aggregate> build(type: Class<T>, id: String, snapshot: Memento?): T {
        var result: T? = null
        try {
            val optional = Arrays.stream(type.constructors).findFirst()
            if (optional.isPresent) {
                val constructor = optional.get()
                val parameters = Arrays.stream(constructor.parameterTypes).map { t: Class<*>? ->
                    if (String::class.java.isAssignableFrom(t)) {
                        return@map id
                    }
                    if (Memento::class.java.isAssignableFrom(t)) {
                        return@map snapshot
                    }
                    _container.getInstance(t)
                }
                result = optional.get().newInstance(*parameters.toArray()) as T
            }
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
        return result!!
    }
}
