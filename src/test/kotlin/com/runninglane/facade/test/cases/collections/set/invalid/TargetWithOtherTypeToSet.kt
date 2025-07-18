package com.runninglane.facade.test.cases.collections.set.invalid

import com.runninglane.facade.test.cases.collections.array.Delegate

interface TargetWithOtherTypeToSet {
    val otherTypeToSet: Set<Delegate.Element>?
}
