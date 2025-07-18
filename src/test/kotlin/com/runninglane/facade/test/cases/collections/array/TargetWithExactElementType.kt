package com.runninglane.facade.test.cases.collections.array

interface TargetWithExactElementType {
    val arrayToList: List<Delegate.Element>
    val listToArray: Array<Delegate.Element>
    val arrayToMutableList: MutableList<Delegate.Element>
    val mutableListToArray: Array<Delegate.Element>

    val setToArray: Array<Delegate.Element>
    val mutableSetToArray: Array<Delegate.Element>

    val arrayToCollection: Collection<Delegate.Element?>
    val collectionToArray: Array<Delegate.Element?>
    val arrayToMutableCollection: MutableCollection<Delegate.Element?>
    val mutableCollectionToArray: Array<Delegate.Element?>
}