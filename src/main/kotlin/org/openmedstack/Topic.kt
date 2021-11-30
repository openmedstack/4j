package org.openmedstack

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Topic constructor(val topic: String){}