package com.runninglane.facade.test.cases.collections.set.unique

data class DelegateWithOtherTypeToSet(
    val arrayToSet: Array<String>,
    val listToSet: List<String>,
    val collectionToSet: Collection<String>
)