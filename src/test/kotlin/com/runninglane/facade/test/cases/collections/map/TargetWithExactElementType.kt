package com.runninglane.facade.test.cases.collections.map

interface TargetWithExactElementType {
    val mapMutableToMutable: MutableMap<String, Delegate.Element>
    val mapImmutableToMutable: Map<String, Delegate.Element>
    val mapMutableToImmutable: MutableMap<Delegate.Element, String>
    val mapImmutableToImmutable: Map<Delegate.Element, Delegate.Element>
}