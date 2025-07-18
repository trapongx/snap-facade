package com.runninglane.facade.test.cases.collections.array

interface TargetWithDifferentElementType {
    val arrayToList: List<Element>
    val listToArray: Array<Element>
    val arrayToMutableList: MutableList<Element>
    val mutableListToArray: Array<Element>

    val arrayToSet: Set<Element>
    val setToArray: Array<Element>
    val arrayToMutableSet: MutableSet<Element>
    val mutableSetToArray: Array<Element>

    val arrayToCollection: Collection<Element?>
    val collectionToArray: Array<Element?>
    val arrayToMutableCollection: MutableCollection<Element?>
    val mutableCollectionToArray: Array<Element?>

    abstract class Element {
        abstract val code: String

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Element) return false
            if (code != other.code) return false
            return true
        }

        override fun hashCode(): Int = code.hashCode()

        override fun toString(): String = "Element(code=$code)"
    }
}