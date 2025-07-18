package com.runninglane.facade.test.cases.elements.collection

interface TargetWithDifferentElementType {
    val listMutableToMutable: MutableList<Element>
    val listImmutableToMutable: MutableList<Element>
    val listMutableToImmutable: List<Element>
    val listImmutableToImmutable: List<Element>

    val setMutableToMutable: MutableSet<Element>
    val setImmutableToMutable: MutableSet<Element>
    val setMutableToImmutable: Set<Element>
    val setImmutableToImmutable: Set<Element>

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