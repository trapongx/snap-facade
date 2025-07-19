package com.runninglane.facade

import kotlin.reflect.KClass

data class From<D : Any>(
    val delegateClass: KClass<D>,
    val delegateTypeParams: Map<String, KClass<*>>,
    val delegate: D,
    val facadeFactory: FacadeFactory
) {
    fun <T : Any> to(targetClass: KClass<T>, targetTypeParams: Map<String, KClass<*>> = emptyMap()): T =
        facadeFactory.create(targetClass, targetTypeParams, delegateClass, delegateTypeParams,  delegate)
}

inline fun <D : Any, reified T : Any> From<D>.to(targetTypeParams: Map<String, KClass<*>> = emptyMap()): T =
    to(T::class, targetTypeParams)
