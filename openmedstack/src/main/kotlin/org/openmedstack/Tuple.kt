package org.openmedstack

/**
 * @see http://www.javatuples.org/ for a more thorough approach
 */
class Tuple<A, B>(val a: A, val b: B) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val tuple = other as Tuple<*, *>
        return if (a != tuple.a) false else b == tuple.b
    }

    override fun hashCode(): Int {
        var result = a.hashCode()
        result = 31 * result + b.hashCode()
        return result
    }
}