package com.runninglane.facade.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Facade(
    val delegateClass: KClass<*>,
    val targetClass: KClass<*>
)

