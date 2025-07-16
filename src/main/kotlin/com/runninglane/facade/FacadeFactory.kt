package com.runninglane.facade

import kotlin.reflect.KClass

class FacadeFactory(
    annotationForPropertyInheriting: Set<KClass<Annotation>> = emptySet()
) {

    private val facadeClassGenerator = FacadeClassGenerator(annotationForPropertyInheriting)

    /**
     * Map of <Pair<targetClass, delegateClass>, facadeClass>.
     * targetClass and delegateClass must be public.
     * targetClass and its properties must be open for inheriting and overriding.
     * targetClass must have an empty constructor.
     * facadeClass generated will have a constructor accepting one delegate.
     */
    private val facadeClassesMap: MutableMap<Pair<KClass<*>, KClass<*>>, KClass<*>> = mutableMapOf()

    fun <T : Any, D : Any> create(
        targetClass: KClass<T>,
        delegateClass: KClass<D>,
        delegate: D
    ): T {
        val facadeClass = facadeClassesMap.getOrPut(targetClass to delegateClass) {
            facadeClassGenerator.generate(targetClass, delegateClass).also { facadeClassesMap[targetClass to delegateClass] = it }
        }
        @Suppress("UNCHECKED_CAST")
        return facadeClass.constructors.first { it.parameters.size == 1 }.call(delegate) as T
    }

    fun <D : Any> from(delegateClass: KClass<D>, delegate: D): From<D> =
        From(delegateClass, delegate, this)

    companion object {
        val default: FacadeFactory = FacadeFactory()
    }
}

inline fun <reified D : Any> FacadeFactory.from(delegate: D): From<D> =
    this.from(D::class, delegate)