package com.runninglane.facade.property.type

import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

internal object CollectionTypeUtils {

    // Collection type classes that need special handling
    private val collectionClasses = setOf(
        Collection::class,
        java.util.Collection::class,
        Map::class,
        java.util.Map::class
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
    fun isCollectionType(type: KClass<*>): Boolean {
        return collectionClasses.any { c -> type.isSubclassOf(c) }
                || type.toString() == "class kotlin.Array"
    }

    /**
     * Determines if a type is a specific kind of collection (List, Set, Map, etc.)
     */
    fun getCollectionTypeCategory(type: KClass<*>): String? {
        val typeName = type.toString()
        return collectionCategories.find { typeName.contains(it) }
    }

}