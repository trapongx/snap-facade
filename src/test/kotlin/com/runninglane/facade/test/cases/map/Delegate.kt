package com.runninglane.facade.test.cases.map

data class Delegate(
    val mapMutableToMutable: MutableMap<String, Element>,
    val mapImmutableToMutable: Map<String, Element?>,
    val mapMutableToImmutable: MutableMap<Element, String>?,
    val mapImmutableToImmutable: Map<Element, Element?>?
) {
    data class Element(val code: String)
}