package org.openmedstack

import com.google.common.collect.Streams
import com.google.common.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.stream.Stream
import kotlin.io.path.*
import kotlin.streams.toList

class ReflectionTool {
    companion object {
        fun <T : Any> getTypeParameter(instanceType: Class<*>, type: Class<T>, index: Int = 0): Type {
            val genericInterfaces = instanceType.genericInterfaces
            val superClass = Arrays.stream(genericInterfaces)
                    .filter { t: Type -> t is ParameterizedType }
                    .map { t: Type -> t as ParameterizedType }
                    .filter { t: ParameterizedType ->
                        t == type || TypeToken.of(t).isSubtypeOf(TypeToken.of(type))
                    }
                    .toArray { length -> arrayOfNulls<ParameterizedType>(length) }

            return superClass[0]!!.actualTypeArguments[index]
        }

        fun isBindableAs(bindingType: Class<*>, instance: Class<*>): Boolean {
            return bindingType.isAssignableFrom(instance) && !Modifier.isAbstract(instance.modifiers) && !Modifier.isInterface(instance.modifiers)
        }

        fun findAllClasses(vararg packages: Package): Stream<Class<*>> {
            val packageNames = Arrays.stream(packages)
                    .map { p -> p.name }.toList()
            return Streams.concat(packageNames.stream().flatMap { n -> findAllClassesUsingClassLoader(n) },
                    packageNames.stream().flatMap { n -> findAllClassesInJar(n) })
        }

        fun findAllClasses(): Stream<Class<*>> {
            val loader = ClassLoader.getSystemClassLoader().definedPackages
            return findAllClasses(*loader)
        }

        private fun findAllClassesInJar(packageName: String): Stream<Class<*>> {
            val cl = ClassLoader.getSystemClassLoader()
            val url = cl.getResource(packageName.replace('.', '/'))
            val path =
                    url.file.substring(0, if (url.file.indexOf('!') == -1) url.file.length else url.file.indexOf('!'))
                            .replace("file:", "")
            val isWindows = System.getProperty("os.name").indexOf("win", ignoreCase = true) > -1
            val p = Path(if (isWindows) path.trimStart('/') else path)
            if (p.isDirectory() || !p.exists() || !p.isReadable()) {
                return Stream.empty()
            }

            val jarFile = JarFile(path)
            val e: Enumeration<JarEntry> = jarFile.entries()

            val urls = arrayOf(URL("jar:file:/$path!/"))
            val ucl = URLClassLoader.newInstance(urls)

            val list = ArrayList<Class<*>>()
            while (e.hasMoreElements()) {
                val je: JarEntry = e.nextElement()

                if (je.isDirectory || !Path(je.name).extension.equals("class", true)) {
                    continue
                }
                // -6 because of .class
                val className: String = je.name.substring(0, je.name.length - 6).replace('/', '.')
                val c = ucl.loadClass(className)
                if (c.packageName == packageName) {
                    list.add(c)
                }
            }
            return list.stream()
        }

        private fun findAllClassesUsingClassLoader(packageName: String): Stream<Class<*>> {
            val stream = ClassLoader.getSystemClassLoader()
                    .getResourceAsStream(packageName.replace(".", "/"))
                    ?: throw NullPointerException("$packageName not found")
            if (stream.available() == 0) {
                return Stream.empty()
            }
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