package com.runninglane.facade.test.cases.collections.set

data class Delegate(
    val setMutableToMutable: MutableSet<Element>,
    val setImmutableToMutable: Set<Element?>,
    val setMutableToImmutable: MutableSet<Element>?,
    val setImmutableToImmutable: Set<Element?>?,
) {
    data class Element(val code: String)
}