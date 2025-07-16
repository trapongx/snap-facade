package com.runninglane.facade

import kotlin.reflect.KClass

class FacadeFactory(
    val annotationForPropertyInheriting: Set<KClass<Annotation>> = emptySet()
) {
    fun <T : Any, D : Any> create(
        targetClass: KClass<T>,
        delegateClass: KClass<D>,
        delegate: D
    ): T {
        TODO()
    }

    fun <D : Any> from(delegateClass: KClass<D>, delegate: D): From<D> =
        From(delegateClass, delegate, this)

    companion object {
        val default: FacadeFactory = FacadeFactory()
    }
}

inline fun <reified D : Any> FacadeFactory.from(delegate: D): From<D> =
    this.from(D::class, delegate)