package org.openmedstack

import com.google.common.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*
import java.util.stream.Stream
import kotlin.collections.ArrayList

class ReflectionTool {
    companion object {
        fun <T: Any> getTypeParameter(instanceType: Class<*>, type: Class<T>, index: Int = 0): Type {
            val genericInterfaces = instanceType.genericInterfaces
            val superClass = Arrays.stream(genericInterfaces)
                .filter { t: Type -> t is ParameterizedType }
                .map{ t: Type -> t as ParameterizedType }
                .filter { t: ParameterizedType ->
                    t == type || TypeToken.of(t).isSubtypeOf(TypeToken.of(type))
                }
                .toArray { length-> arrayOfNulls<ParameterizedType>(length) }

            return superClass[0]!!.actualTypeArguments[index]
        }

        fun findAllClasses(vararg packages:Package): Stream<Class<*>> {
            return Arrays.stream(packages)
                .map { p -> p.name }
                .flatMap { n -> findAllClassesUsingClassLoader(n) }
        }

        fun findAllClasses(): Stream<Class<*>> {
            val loader = ClassLoader.getSystemClassLoader().definedPackages
            return Arrays.stream(loader)
                .map { p -> p.name }
                .flatMap { n -> findAllClassesUsingClassLoader(n) }
        }

        private fun findAllClassesUsingClassLoader(packageName: String): Stream<Class<*>> {
            val stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replace("[.]".toRegex(), "/"))
            val reader = BufferedReader(InputStreamReader(stream))
            val lines = reader.readLines()
            return lines.stream()
                .filter { line: String -> line.endsWith(".class") }
                .filter { line -> !line.isNullOrBlank() }
                .map { line: String -> getClass(line, packageName) }
        }

        private fun getClass(className: String, packageName: String): Class<*>? {
            try {
                return Class.forName(
                    packageName + "."
                            + className.substring(0, className.lastIndexOf('.'))
                )
            } catch (e: ClassNotFoundException) {
                // handle the exception
            }
            return null
        }
    }
}