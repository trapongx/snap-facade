package com.runninglane.facade.test.cases.collection

data class Delegate(
    val listMutableToMutable: MutableList<Element>,
    val listImmutableToMutable: List<Element?>,
    val listMutableToImmutable: MutableList<Element>?,
    val listImmutableToImmutable: List<Element?>?,

    val setMutableToMutable: MutableSet<Element>,
    val setImmutableToMutable: Set<Element?>,
    val setMutableToImmutable: MutableSet<Element>?,
    val setImmutableToImmutable: Set<Element?>?,
) {
    data class Element(val code: String)
}