package com.runninglane.facade

import kotlin.reflect.KClass

class FacadeFactory(
    val facadeClassGenerator: FacadeClassGenerator = FacadeClassGenerator()
) {

    constructor(annotationForPropertyInheriting: Set<KClass<Annotation>>) : this(
        FacadeClassGenerator(
            annotationForPropertyInheriting = annotationForPropertyInheriting
        )
    )

    /**
     * Map of <Pair<targetClass, delegateClass>, facadeClass>.
     * targetClass and delegateClass must be public.
     * targetClass and its properties must be open for inheriting and overriding.
     * targetClass, if not interface, must have an empty constructor.
     * facadeClass generated will have a constructor accepting one delegate.
     */
    data class FacadeClassKey(
        val targetClass: KClass<*>,
        val targetTypeParams: Map<String, KClass<*>>,
        val delegateClass: KClass<*>,
        val delegateTypeParams: Map<String, KClass<*>>
    )
    private val facadeClassesMap: MutableMap<FacadeClassKey, KClass<*>> = mutableMapOf()

    fun <T : Any, D : Any> create(
        targetClass: KClass<T>,
        targetTypeParams: Map<String, KClass<*>>,
        delegateClass: KClass<D>,
        delegateTypeParams: Map<String, KClass<*>>,
        delegate: D
    ): T {
        val facadeClass = facadeClassesMap.getOrPut(
            FacadeClassKey(targetClass, targetTypeParams, delegateClass, delegateTypeParams)
        ) {
            facadeClassGenerator.generate(
                targetClass,
                targetTypeParams,
                delegateClass,
                delegateTypeParams
            )
        }
        @Suppress("UNCHECKED_CAST")
        return facadeClass.constructors.first { it.parameters.size == 2 }.call(delegate, this) as T
    }

    fun <D : Any> from(delegateClass: KClass<D>, delegate: D, delegateTypeParams: Map<String, KClass<*>> = emptyMap()): From<D> =
        From(delegateClass, delegateTypeParams, delegate, this)

    companion object {
        val default: FacadeFactory = FacadeFactory()
    }
}

inline fun <reified D : Any> FacadeFactory.from(delegate: D, delegateTypeParams: Map<String, KClass<*>> = emptyMap()): From<D> =
    this.from(D::class, delegate, delegateTypeParams)
