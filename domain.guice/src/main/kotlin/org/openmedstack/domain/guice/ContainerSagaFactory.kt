package org.openmedstack.domain.guice

import com.google.inject.Inject
import com.google.inject.Injector
import org.openmedstack.domain.Saga
import org.openmedstack.eventstore.IConstructSagas
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*

internal class ContainerSagaFactory @Inject constructor(private val _container: Injector) : IConstructSagas {
    override fun <T : Saga> build(type: Class<T>, id: String): T {
        try {
            val constructor: Constructor<T> = type.constructors[0] as Constructor<T>
            val parameters = Arrays.stream(constructor.parameterTypes).map { t: Class<*> ->
                if (String::class.java.isAssignableFrom(t)) {
                    return@map id
                }
                _container.getInstance(t)
            }
            return constructor.newInstance(*parameters.toArray())
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
    }
}
