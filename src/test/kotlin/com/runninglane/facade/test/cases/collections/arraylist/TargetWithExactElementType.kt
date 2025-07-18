package com.runninglane.facade.test.cases.collections.arraylist


interface TargetWithExactElementType {
    val arrayToArray: Array<Delegate.Element>
    val arrayToArrayList: Array<Delegate.Element?>
    val arrayListToArrayList: ArrayList<Delegate.Element>?
    val arrayListToArray: ArrayList<Delegate.Element?>?
}