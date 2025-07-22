package com.runninglane.facade.test.cases.compatibility

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CompatibilityTest {

    @Test
    fun `should correctly create facade from same nested delegate`() {
        val delegate = Delegate(nested = NestedDelegate("Test"))
        val facade = FacadeFactory.default.from(delegate).to(TargetWithSameNested::class)
        assertSame(delegate.nested, facade.nested)
    }

    @Test
    fun `should correctly create facade from compatible nested delegate`() {
        val delegate = Delegate(nested = NestedDelegate("Test"))
        val facade = FacadeFactory.default.from(delegate).to(TargetWithCompatibleNested::class)
        assertSame(delegate.nested, facade.nested)
    }

    @Test
    fun `should correctly create facade from incompatible nested delegate`() {
        val delegate = Delegate(nested = NestedDelegate("Test"))
        val facade = FacadeFactory.default.from(delegate).to(TargetWithIncompatibleNested::class)
        assertEquals(delegate.nested.name, facade.nested.name)
    }
}