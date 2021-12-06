package org.openmedstack

class EnvironmentTopicProvider constructor(private val _tenantProvider: IProvideTenant, private val _topicMap: Map<String, String>? = null) :
    IProvideTopic {
    override fun <T> get(type: Class<T>): String {
        var tenant = _tenantProvider.tenantName;
        var fullName = type.typeName
        if (_topicMap?.contains(fullName) == true) {
            return tenant + _topicMap[fullName];
        }

        val topicType = Topic::class.java
        var topic = if (type.isAnnotationPresent(topicType)) type.getAnnotation(topicType).topic else fullName
        return tenant + topic
    }
}