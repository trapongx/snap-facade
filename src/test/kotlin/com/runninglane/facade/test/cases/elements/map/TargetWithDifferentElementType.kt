package com.runninglane.facade.test.cases.elements.map

interface TargetWithDifferentElementType {
    val mapMutableToMutable: MutableMap<String, Element>
    val mapImmutableToMutable: MutableMap<String, Element>
    val mapMutableToImmutable: Map<Element, String>
    val mapImmutableToImmutable: Map<Element, Element>

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