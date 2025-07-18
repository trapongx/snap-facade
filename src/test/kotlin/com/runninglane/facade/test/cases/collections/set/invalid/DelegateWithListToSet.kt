package com.runninglane.facade.test.cases.collections.set.invalid

import com.runninglane.facade.test.cases.collections.set.Delegate

data class DelegateWithListToSet(
    val otherTypeToSet: MutableList<Delegate.Element>
)