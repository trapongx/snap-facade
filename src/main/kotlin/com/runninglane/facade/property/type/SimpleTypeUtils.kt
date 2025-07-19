package com.runninglane.facade.property.type

import kotlin.reflect.KClass
import kotlin.reflect.KType

internal object SimpleTypeUtils {

    // Simple type packages that we consider as primitive or simple types
    private val simpleTypePackages = setOf(
        "kotlin",
        "java.lang",
        "java.math",
        "java.time",
        "java.util.UUID"
    )

    /**
     * Determines if a type is considered a simple type (primitive, String, enum, etc.)
     */
    fun isSimpleType(type: KClass<*>): Boolean {
        val typeName = type.toString()

        // Check if it's in a simple type package
        if (simpleTypePackages.any { typeName.startsWith("class $it.") }) {
            return true
        }

        // Check if it's an enum (safely)
        if (typeName.startsWith("class ")) {
            try {
                val className = typeName.substring(6)
                return Class.forName(className).isEnum
            } catch (_: ClassNotFoundException) {
                // If class not found, it's not a simple type
                return false
            }
        }

        return false
    }

    fun isSimpleType(type: KType): Boolean = isSimpleType(type.classifier as KClass<*>)
}