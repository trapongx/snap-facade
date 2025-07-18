package com.runninglane.facade.test.cases.collection.delegate

data class Delegate(
    val listMutableToMutable: MutableList<Element>,
    val listImmutableToMutable: List<Element?>,
    val listMutableToImmutable: MutableList<Element>?,
    val listImmutableToImmutable: List<Element?>?,

    val setMutableToMutable: MutableSet<Element>,
    val setImmutableToMutable: Set<Element?>,
    val setMutableToImmutable: MutableSet<Element>?,
    val setImmutableToImmutable: Set<Element?>?,

    val mapMutableToMutable: MutableMap<String, Element>,
    val mapImmutableToMutable: Map<String, Element?>,
    val mapMutableToImmutable: MutableMap<Element, String>?,
    val mapImmutableToImmutable: Map<Element, Element?>?
) {
    data class Element(val char: Char)
}