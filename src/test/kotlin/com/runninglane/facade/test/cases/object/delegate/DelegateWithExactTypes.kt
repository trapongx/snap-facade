package com.runninglane.facade.test.cases.`object`.delegate

import com.runninglane.facade.test.cases.`object`.target.InnerTargetWithSimpleValues

data class DelegateWithExactTypes(
    var inner: InnerTargetWithSimpleValues?
)