package com.runninglane.facade.test.cases.elements.collection

interface TargetWithExactElementType {
    val listMutableToMutable: MutableList<Delegate.Element>
    val listImmutableToMutable: MutableList<Delegate.Element>
    val listMutableToImmutable: List<Delegate.Element>
    val listImmutableToImmutable: List<Delegate.Element>

    val setMutableToMutable: MutableSet<Delegate.Element>
    val setImmutableToMutable: MutableSet<Delegate.Element>
    val setMutableToImmutable: Set<Delegate.Element>
    val setImmutableToImmutable: Set<Delegate.Element>
}