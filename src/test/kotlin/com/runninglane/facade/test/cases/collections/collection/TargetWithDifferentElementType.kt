package com.runninglane.facade.test.cases.collections.collection

interface TargetWithDifferentElementType {
    val collectionMutableToMutable: MutableCollection<Element>
    val collectionImmutableToMutable: MutableCollection<Element>
    val collectionMutableToImmutable: Collection<Element>
    val collectionImmutableToImmutable: Collection<Element>

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