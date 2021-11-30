package org.openmedstack

import com.google.common.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

class ReflectionTool {
    companion object {
        fun <T: Any> getTypeParameter(instance: Any, type: Class<T>, index: Int = 0): Type {
            val `class` = instance.javaClass
            val genericInterfaces = `class`.genericInterfaces
            val superClass = Arrays.stream(genericInterfaces)
                .filter { t: Type -> t is ParameterizedType }
                .map{ t: Type -> t as ParameterizedType }
                .filter { t: ParameterizedType -> TypeToken.of(t).isSubtypeOf(TypeToken.of(type)) }
                .toArray { length-> arrayOfNulls<ParameterizedType>(length) }

            return superClass[0]!!.actualTypeArguments[index]
        }
    }
}