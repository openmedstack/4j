package org.openmedstack

interface IMapTopics {
    operator fun get(index: String): String?
    operator fun set(index: String, value: String): Unit
    fun contains(index: String): Boolean
}

class HashMapTopics : IMapTopics {
    private val _items = HashMap<String, String>()
    override fun get(index: String): String? {
        return _items[index]
    }

    override operator fun set(index: String, value: String) {
        _items[index] = value
    }

    override operator fun contains(index: String): Boolean {
        return _items.containsKey(index)
    }
}

class EnvironmentTopicProvider constructor(
    private val _tenantProvider: IProvideTenant,
    private val _topicMap: IMapTopics
) :
    IProvideTopic {
    override fun <T> get(type: Class<T>): String {
        val canonical = getCanonical(type);
        var tenant = _tenantProvider.tenantName;
        return tenant + canonical
    }

    override fun <T> getCanonical(type: Class<T>): String {
        var fullName = type.typeName
        if (_topicMap.contains(fullName)) {
            return _topicMap[fullName]!!;
        }

        val topicType = Topic::class.java
        return if (type.isAnnotationPresent(topicType)) type.getAnnotation(topicType).topic else fullName
    }
}