package com.runninglane.facade.property.decorator.collection

import com.runninglane.facade.property.type.TypeNameResolver
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
 * Supported Set implementations include:
 * - Set
 * - MutableSet
 * - HashSet
 * - LinkedHashSet
 * - SortedSet
 * - TreeSet
 *
 * Supported List and Queue implementations include:
 * - List/MutableList
 * - ArrayList
 * - LinkedList
 * - Deque/ArrayDeque (double-ended queue)
 * - Queue
 * - Stack
 * - Vector
 *
 * Supported Map implementations include:
 * - Map/MutableMap
 * - SortedMap
 * - TreeMap
 *
 * @return String representing the conversion method call, empty string if no conversion needed,
 *         or null if conversion is not supported
 */
internal object CollectionConverter : Converter {

    private fun expressionWithDistinctCheck(expression: String) = Pair(
        "let { require(it.distinct().size == it.size) { %S }; $expression }",
        listOf("Cannot convert non-unique collection to Set")
    )

    private val mapFromNonSet = mapOf(
        "kotlin.collections.MutableSet" to expressionWithDistinctCheck("it.toMutableSet()"),
        "kotlin.collections.Set" to expressionWithDistinctCheck("it.toSet()"),
        "kotlin.collections.HashSet" to expressionWithDistinctCheck("it.toHashSet()"),
        "kotlin.collections.LinkedHashSet" to expressionWithDistinctCheck("LinkedHashSet(it)"),
        "java.util.SortedSet" to expressionWithDistinctCheck("java.util.TreeSet(it)"),
        "java.util.TreeSet" to expressionWithDistinctCheck("java.util.TreeSet(it)")
    )

    private val mapFromSet = mapOf(
        "kotlin.collections.MutableSet" to "toMutableSet()",
        "kotlin.collections.Set" to "toSet()",
        "kotlin.collections.HashSet" to "toHashSet()",
        "kotlin.collections.LinkedHashSet" to "let { LinkedHashSet(it) }",
        "java.util.SortedSet" to "let { java.util.TreeSet(it) }",
        "java.util.TreeSet" to "let { java.util.TreeSet(it) }"
    ).mapValues { (_, v) -> v to emptyList<String>() }

    private val mapFromCollection = mapOf(
        "kotlin.collections.Collection" to "toList()",
        "kotlin.collections.MutableCollection" to "toMutableList()",
        "kotlin.collections.List" to "toList()",
        "kotlin.collections.MutableList" to "toMutableList()",
        "kotlin.Array" to "toTypedArray()",
        "kotlin.collections.ArrayList" to "let { ArrayList(it) }",
        "java.util.LinkedList" to "let { java.util.LinkedList(it) }",
        "kotlin.collections.LinkedList" to "let { java.util.LinkedList(it) }",
        "kotlin.collections.ArrayDeque" to "let { ArrayDeque(it) }",
        "java.util.ArrayDeque" to "let { java.util.ArrayDeque(it) }",
        "kotlin.collections.Deque" to "let { ArrayDeque(it) }",
        "java.util.Deque" to "let { java.util.ArrayDeque(it) }",
        "java.util.Queue" to "let { java.util.LinkedList(it) }",
        "java.util.Vector" to "let { java.util.Vector(it) }"
    ).mapValues { (_, v) -> v to emptyList<String>() }

    private val mapFromCollectionNeedElementType = mapOf(
        "java.util.Stack" to "let { java.util.Stack<E>().also { stack -> it.forEach { stack.push(it) } } }"
    ).mapValues { (_, v) -> v to emptyList<String>() }

    override fun convert(from: KType, fromStr: String, to: KType, toStr: String): Pair<String, List<String>>? {
        return when (from.jvmErasure.isSubclassOf(Set::class)) {
            true -> mapFromSet[toStr]
            false -> mapFromNonSet[toStr]
        } ?: mapFromCollection[toStr] ?: mapFromCollectionNeedElementType[toStr]?.let {
            val elementType = to.arguments[0].type!!
            val elementTypeResolved = TypeNameResolver.resolve(elementType, emptyMap())
            Pair(it.first.replace("<E>", "<$elementTypeResolved>"), it.second)
        }
    }
}