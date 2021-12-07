package org.openmedstack.messaging.rabbitmq

class ManualResetEvent(open: Boolean = false) {
    private val monitor = Object()

    @Volatile
    private var open = false

    @Throws(InterruptedException::class)
    fun waitOne() {
        synchronized(monitor) {
            while (!open) {
                monitor.wait()
            }
        }
    }

    fun set() { //open start
        synchronized(monitor) {
            open = true
            monitor.notifyAll()
        }
    }

    fun reset() { //close stop
        open = false
    }

    init {
        this.open = open
    }
}