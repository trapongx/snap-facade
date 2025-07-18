package com.runninglane.facade.test.cases.collections.list

interface TargetWithExactElementType {
    val listMutableToMutable: MutableList<Delegate.Element>
    val listImmutableToMutable: MutableList<Delegate.Element>
    val listMutableToImmutable: List<Delegate.Element>
    val listImmutableToImmutable: List<Delegate.Element>
}