package com.runninglane.facade

import kotlin.reflect.KClass

data class From<D : Any>(
    val delegateClass: KClass<D>,
    val delegate: D,
    val facadeFactory: FacadeFactory
) {
    fun <T : Any> to(targetClass: KClass<T>): T = facadeFactory.create(targetClass, delegateClass, delegate)
}

inline fun <D : Any, reified T : Any> From<D>.to(): T = to(T::class)