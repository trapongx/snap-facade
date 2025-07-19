package com.runninglane.facade.property.type

import java.util.*
import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

internal object CollectionTypeUtils {

    // Collection type classes that need special handling
    private val collectionClasses = setOf(
        Collection::class,
        Array::class,
        Map::class,
        Deque::class,
        Queue::class,
        Stack::class,
        Vector::class
    )

    private val collectionCategories = setOf(
        "List",
        "Set",
        "Map",
        "Array",
        "Collection",
        "Deque",
        "Queue",
        "Stack",
        "Vector",
    )
    /**
     * Determines if a type is a collection type
     */
    fun isCollectionType(type: KType): Boolean {
        return collectionClasses.any { c -> type.jvmErasure.isSubclassOf(c) }
                || type.toString().startsWith("kotlin.Array<")
    }

    /**
     * Determines if a type is a specific kind of collection (List, Set, Map, etc.)
     */
    fun getCollectionTypeCategory(type: KType): String? {
        val typeName = type.classifier.toString()
        return collectionCategories.find { typeName.contains(it) }
    }

}