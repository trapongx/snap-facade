package com.runninglane.facade.test.cases.collection.target

import com.runninglane.facade.test.cases.collection.delegate.Delegate

interface TargetWithExactElementType {
    val listMutableToMutable: MutableList<Delegate.Element>
    val listImmutableToMutable: MutableList<Delegate.Element>
    val listMutableToImmutable: List<Delegate.Element>
    val listImmutableToImmutable: List<Delegate.Element>
    val mapMutableToMutable: MutableMap<String, Delegate.Element>
    val mapImmutableToMutable: MutableMap<String, Delegate.Element>
    val mapMutableToImmutable: Map<String, Delegate.Element>
    val mapImmutableToImmutable: Map<String, Delegate.Element>
    val setMutableToMutable: MutableSet<Delegate.Element>
    val setImmutableToMutable: MutableSet<Delegate.Element>
    val setMutableToImmutable: Set<Delegate.Element>
    val setImmutableToImmutable: Set<Delegate.Element>
    val array: Array<Delegate.Element>
    val arrayList: ArrayList<Delegate.Element>
    val linkedHashSet: LinkedHashSet<Delegate.Element>
    val hashSet: HashSet<Delegate.Element>
    val hashMap: HashMap<String, Delegate.Element>
    val linkedHashMap: LinkedHashMap<String, Delegate.Element>
    val iterator: Iterator<Delegate.Element>
    val sequence: Sequence<Delegate.Element>
    val pair: Pair<String, Delegate.Element>
    val triple: Triple<String, String, Delegate.Element>
}