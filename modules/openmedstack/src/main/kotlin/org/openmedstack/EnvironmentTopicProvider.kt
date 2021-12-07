package org.openmedstack

class EnvironmentTopicProvider constructor(
    private val _tenantProvider: IProvideTenant,
    private val _topicMap: Map<String, String>? = null
) :
    IProvideTopic {
    override fun <T> get(type: Class<T>): String {
        val canonical = getCanonical(type);
        var tenant = _tenantProvider.tenantName;
        return tenant + canonical
    }

    override fun <T> getCanonical(type: Class<T>): String {
        var fullName = type.typeName
        if (_topicMap?.contains(fullName) == true) {
            return _topicMap[fullName]!!;
        }

        val topicType = Topic::class.java
        return if (type.isAnnotationPresent(topicType)) type.getAnnotation(topicType).topic else fullName
    }
}