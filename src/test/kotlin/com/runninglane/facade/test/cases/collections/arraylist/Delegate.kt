package com.runninglane.facade.test.cases.collections.arraylist

data class Delegate(
    val arrayToArray: Array<Element>,
    val arrayToArrayList: Array<Element?>,
    val arrayListToArrayList: ArrayList<Element>?,
    val arrayListToArray: ArrayList<Element?>?,
) {
    data class Element(val code: String)
}