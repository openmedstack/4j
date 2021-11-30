package org.openmedstack

class MessageHeadersImpl(items: Map<String, Any>) : MessageHeaders {
    private val _headers: MutableMap<String, Any>

    override val userToken: String?
        get() = _headers["token"] as String?

    override val size: Int
        get() = _headers.size

    override fun isEmpty(): Boolean {
        return _headers.isEmpty()
    }

    override fun containsKey(key: String): Boolean {
        return _headers.containsKey(key)
    }

    override fun containsValue(value: Any): Boolean {
        return _headers.containsValue(value)
    }

    override operator fun get(key: String): Any? {
        return _headers[key]
    }

    override val keys: MutableSet<String>
        get()= _headers.keys

    override val values: MutableCollection<Any>
        get() = _headers.values

    override val entries : MutableSet<MutableMap.MutableEntry<String, Any>>
        get() = _headers.entries

    init {
        _headers = items.toMutableMap()
    }
}