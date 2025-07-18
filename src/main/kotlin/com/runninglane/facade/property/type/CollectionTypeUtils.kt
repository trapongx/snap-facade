package com.runninglane.facade.property.type

import kotlin.reflect.KProperty1
import kotlin.reflect.KType

internal object CollectionTypeUtils {

    // Collection type classes that need special handling
    private val collectionTypes = setOf(
        List::class,
        Set::class,
        Map::class,
        Collection::class,
        Array::class
    ).flatMap { listOf(it.qualifiedName, it.java.canonicalName) }.toSet()

    // Mutable collection type classes
    private val mutableCollectionTypes = setOf(
        MutableList::class,
        MutableSet::class,
        MutableMap::class,
        MutableCollection::class,
        Array::class,
        ArrayList::class
    ).flatMap { listOf(it.qualifiedName, it.java.canonicalName) }.toSet()

    /**
     * Determines if a type is a collection type
     */
    fun isCollectionType(type: KType): Boolean {
        val typeName = type.classifier.toString()

        // Check for collection names as plain interfaces/classes (not prefixed with 'class')
        val collectionNames = listOf("List", "Set", "Map", "Collection")
        if (collectionNames.any { typeName.endsWith(it) }) {
            return true
        }

        if (typeName.startsWith("class ") || typeName.startsWith("interface ")) {
            val prefix = if (typeName.startsWith("class ")) "class " else "interface "
            val className = typeName.substring(prefix.length)

            // Direct match with known collection types
            if (className in collectionTypes || className in mutableCollectionTypes) {
                return true
            }

            // Check interfaces safely
            try {
                val clazz = Class.forName(className)
                return clazz.interfaces.any { i ->
                    i.name in collectionTypes || i.name in mutableCollectionTypes
                }
            } catch (_: ClassNotFoundException) {
                // If class not found, assume it's not a collection
                return false
            }
        }
        return false
    }

    /**
     * Determines if a type is a specific kind of collection (List, Set, Map, etc.)
     */
    fun getCollectionTypeCategory(type: KType): String? {
        val typeName = type.classifier.toString()
        if (typeName.startsWith("class ") || typeName.startsWith("interface ")) {
            val prefix = if (typeName.startsWith("class ")) "class " else "interface "
            val className = typeName.substring(prefix.length)

            // Check if it's a List, Set, Map, etc.
            return when {
                className.contains("List") -> "List"
                className.contains("Set") -> "Set"
                className.contains("Map") -> "Map"
                className.contains("Collection") -> "Collection"
                className.contains("Array") -> "Array"
                else -> null
            }
        }

        return null
    }

    /**
     * Checks if a property's return type string indicates it's a mutable or immutable collection
     */
    fun isMutableCollectionType(property: KProperty1<*, *>): Boolean {
        val returnTypeStr = property.returnType.toString()

        return returnTypeStr.contains("kotlin.collections.MutableList") ||
                returnTypeStr.contains("kotlin.collections.MutableSet") ||
                returnTypeStr.contains("kotlin.collections.MutableMap") ||
                returnTypeStr.contains("kotlin.collections.MutableCollection") ||
                returnTypeStr.contains("kotlin.Array") ||
                returnTypeStr.contains("kotlin.ArrayList")
    }
}