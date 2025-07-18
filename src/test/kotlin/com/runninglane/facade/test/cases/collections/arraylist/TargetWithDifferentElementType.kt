package com.runninglane.facade.test.cases.collections.arraylist

interface TargetWithDifferentElementType {
    val arrayToArray: Array<Element>
    val arrayToArrayList: Array<Element?>
    val arrayListToArrayList: ArrayList<Element>?
    val arrayListToArray: ArrayList<Element?>?

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