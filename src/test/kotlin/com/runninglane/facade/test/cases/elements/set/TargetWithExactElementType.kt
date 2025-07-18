package com.runninglane.facade.test.cases.elements.set

interface TargetWithExactElementType {
    val setMutableToMutable: MutableSet<Delegate.Element>
    val setImmutableToMutable: MutableSet<Delegate.Element>
    val setMutableToImmutable: Set<Delegate.Element>
    val setImmutableToImmutable: Set<Delegate.Element>
}