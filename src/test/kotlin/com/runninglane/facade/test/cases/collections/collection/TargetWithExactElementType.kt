package com.runninglane.facade.test.cases.collections.collection

interface TargetWithExactElementType {
    val collectionMutableToMutable: MutableCollection<Delegate.Element>
    val collectionImmutableToMutable: MutableCollection<Delegate.Element>
    val collectionMutableToImmutable: Collection<Delegate.Element>
    val collectionImmutableToImmutable: Collection<Delegate.Element>
}