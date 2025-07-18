package com.runninglane.facade.property.decorator.collection

import kotlin.reflect.KType
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

/**
 * Converts collections between different collection types.
 *
 * This converter handles conversions between different collection types with the following rules:
 * 
 * - Non-Set collections (List, Array, Collection) CAN be converted to Set types, but with an important
 *   requirement: the source collection must contain only unique elements. This is enforced at runtime
 *   with a validation check to prevent accidental data loss.
 *
 * - Set CAN be converted to other collection types (List, Array, etc.) as this preserves all elements
 *   without any data loss or additional requirements.
 *
 * Note: This converter assumes element type compatibility has already been verified.
 *
 * Examples of supported conversions:
 * - Set -> List: allowed (preserves all elements)
 * - Set -> Array: allowed (preserves all elements)
 * - List -> Array: allowed (preserves all elements)
 * - List -> Set: allowed ONLY if all elements in the List are unique
 * - Array -> Set: allowed ONLY if all elements in the Array are unique
 *
 * @return String representing the conversion method call, empty string if no conversion needed,
 *         or null if conversion is not supported
 */
internal object CollectionConverter : Converter {

    private val mapFromNonSet = mapOf(
        "kotlin.collections.MutableSet" to "let { require(it.distinct().size == it.size) { \"Cannot convert non-unique collection to set\" }; it.toMutableSet() }",
        "kotlin.collections.Set" to "let { require(it.distinct().size == it.size) { \"Cannot convert non-unique collection to set\" }; it.toSet() }"
    )

    private val mapFromSet = mapOf(
        "kotlin.collections.MutableSet" to "toMutableSet()",
        "kotlin.collections.Set" to "toSet()"
    )

    private val mapFromCollection = mapOf(
        "kotlin.collections.Collection" to "toList()",
        "kotlin.collections.MutableCollection" to "toMutableList()",
        "kotlin.collections.List" to "toList()",
        "kotlin.collections.MutableList" to "toMutableList()",
        "kotlin.Array" to "toTypedArray()",
        "kotlin.collections.ArrayList" to "let { ArrayList(it) }"
    )

    override fun convert(from: KType, fromStr: String, to: KType, toStr: String): String? {
        return when (from.jvmErasure.isSubclassOf(Set::class)) {
            true -> mapFromSet[toStr]
            false -> mapFromNonSet[toStr]
        } ?: mapFromCollection[toStr]
    }
}