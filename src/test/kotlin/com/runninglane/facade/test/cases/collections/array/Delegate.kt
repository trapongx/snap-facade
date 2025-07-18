package com.runninglane.facade.test.cases.collections.array

data class Delegate(
    val arrayToList: Array<Element>,
    val listToArray: List<Element>,
    val arrayToMutableList: Array<Element>,
    val mutableListToArray: MutableList<Element>,

    val arrayToSet: Array<Element>,
    val setToArray: Set<Element>,
    val arrayToMutableSet: Array<Element>,
    val mutableSetToArray: MutableSet<Element>,

    val arrayToCollection: Array<Element?>,
    val collectionToArray: Collection<Element?>,
    val arrayToMutableCollection: Array<Element?>,
    val mutableCollectionToArray: MutableCollection<Element?>
) {
    data class Element(val code: String)
}