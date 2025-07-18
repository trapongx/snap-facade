package com.runninglane.facade.test.cases.collections.list

data class Delegate(
    val listMutableToMutable: MutableList<Element>,
    val listImmutableToMutable: List<Element?>,
    val listMutableToImmutable: MutableList<Element>?,
    val listImmutableToImmutable: List<Element?>?,
) {
    data class Element(val code: String)
}