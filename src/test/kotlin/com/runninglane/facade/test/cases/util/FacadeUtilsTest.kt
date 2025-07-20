package com.runninglane.facade.test.cases.util

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import com.runninglane.facade.util.FacadeUtils
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class FacadeUtilsTest {

    @Test
    fun `should correctly return delegate from facade`() {
        val delegate = Delegate(name = "Test")
        val facade = FacadeFactory.default.from(delegate).to(Target::class)

        val annotation = FacadeUtils.getAnnotation(facade)

        assertSame(Delegate::class, annotation?.delegateClass)
        assertSame(Target::class, annotation?.targetClass)
    }

}
