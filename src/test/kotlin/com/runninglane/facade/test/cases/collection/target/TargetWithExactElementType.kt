package com.runninglane.facade.test.cases.collection.target

import com.runninglane.facade.test.cases.collection.delegate.Delegate

interface TargetWithExactElementType {
    val listMutableToMutable: MutableList<Delegate.Element>
    val listImmutableToMutable: MutableList<Delegate.Element>
    val listMutableToImmutable: List<Delegate.Element>
    val listImmutableToImmutable: List<Delegate.Element>

    val setMutableToMutable: MutableSet<Delegate.Element>
    val setImmutableToMutable: MutableSet<Delegate.Element>
    val setMutableToImmutable: Set<Delegate.Element>
    val setImmutableToImmutable: Set<Delegate.Element>

    val mapMutableToMutable: MutableMap<String, Delegate.Element>
    val mapImmutableToMutable: Map<String, Delegate.Element>
    val mapMutableToImmutable: MutableMap<Delegate.Element, String>
    val mapImmutableToImmutable: Map<Delegate.Element, Delegate.Element>
}