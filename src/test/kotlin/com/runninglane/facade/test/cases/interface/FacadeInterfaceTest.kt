package com.runninglane.facade.test.cases.`interface`

import com.runninglane.facade.Facade
import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class FacadeInterfaceTest {

    @Test
    fun `should correctly return delegate from facade`() {
        val delegate = Delegate(name = "Test")
        val facade = FacadeFactory.default.from(delegate).to(Target::class)

        assertSame(delegate, (facade as Facade<*>).delegate)
    }

}
