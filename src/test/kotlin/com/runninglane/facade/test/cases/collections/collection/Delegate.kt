package com.runninglane.facade.test.cases.collections.collection

data class Delegate(
    val collectionMutableToMutable: MutableCollection<Element>,
    val collectionImmutableToMutable: Collection<Element?>,
    val collectionMutableToImmutable: MutableCollection<Element>?,
    val collectionImmutableToImmutable: Collection<Element?>?,
) {
    data class Element(val code: String)
}