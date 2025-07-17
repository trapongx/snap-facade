package com.runninglane.facade.test.cases.collection.target

interface TargetWithDifferentElementType {
    val listMutableToMutable: MutableList<Element>
    val listImmutableToMutable: MutableList<Element>
    val listMutableToImmutable: List<Element>
    val listImmutableToImmutable: List<Element>
    val mapMutableToMutable: MutableMap<String, Element>
    val mapImmutableToMutable: MutableMap<String, Element>
    val mapMutableToImmutable: Map<String, Element>
    val mapImmutableToImmutable: Map<String, Element>
    val setMutableToMutable: MutableSet<Element>
    val setImmutableToMutable: MutableSet<Element>
    val setMutableToImmutable: Set<Element>
    val setImmutableToImmutable: Set<Element>
    val array: Array<Element>
    val arrayList: ArrayList<Element>
    val linkedHashSet: LinkedHashSet<Element>
    val hashSet: HashSet<Element>
    val hashMap: HashMap<String, Element>
    val linkedHashMap: LinkedHashMap<String, Element>
    val iterator: Iterator<Element>
    val sequence: Sequence<Element>
    val pair: Pair<String, Element>
    val triple: Triple<String, String, Element>

    class Element(val char: Char)
}