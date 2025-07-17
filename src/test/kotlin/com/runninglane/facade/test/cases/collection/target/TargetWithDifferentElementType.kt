package com.runninglane.facade.test.cases.collection.target

interface TargetWithDifferentElementType {
    val listMutableToMutable: MutableList<Element>
    val listImmutableToMutable: MutableList<Element>
    val listMutableToImmutable: List<Element>
    val listImmutableToImmutable: List<Element>

    val setMutableToMutable: MutableSet<Element>
    val setImmutableToMutable: MutableSet<Element>
    val setMutableToImmutable: Set<Element>
    val setImmutableToImmutable: Set<Element>

    interface Element {
        val char: Char
    }
}