package com.runninglane.facade.property.decorator.collection

import kotlin.reflect.KType

/**
 * Converts maps between different map types.
 *
 * This converter handles conversions between map types with one important restriction:
 * non-Set collections (List, Array, Collection) CANNOT be converted to Set types.
 * This restriction exists because converting to Set would potentially cause data loss
 * due to Set's uniqueness property removing duplicate elements.
 *
 * However, Set CAN be converted to other collection types (List, Array, etc.)
 * as this preserves all elements without data loss.
 *
 * Note: This converter assumes element type compatibility has already been verified.
 *
 * Examples of supported conversions:
 * - Set -> List: allowed (preserves all elements)
 * - Set -> Array: allowed (preserves all elements)
 * - List -> Array: allowed (preserves all elements)
 * - List -> Set: NOT allowed (might lose duplicate elements)
 *
 * @return String representing the conversion method call, empty string if no conversion needed,
 *         or null if conversion is not supported
 */
internal object MapConverter : Converter {

    private val map = mapOf(
        "kotlin.collections.Map" to "toMap()",
        "kotlin.collections.MutableMap" to "toMutableMap()",
        "kotlin.collections.HashMap" to "let { HashMap(it) }",
        "java.util.HashMap" to "let { HashMap(it) }",
        "java.util.TreeMap" to "let { java.util.TreeMap(it) }",
        "java.util.SortedMap" to "let { java.util.TreeMap(it) }"
    ).mapValues { (_, v) -> v to emptyList<String>() }

    override fun convert(from: KType, fromStr: String, to: KType, toStr: String): Pair<String, List<String>>? {
        return map[toStr]
    }
}