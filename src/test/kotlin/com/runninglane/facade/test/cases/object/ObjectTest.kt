package com.runninglane.facade.test.cases.`object`

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import com.runninglane.facade.test.cases.`object`.delegate.DelegateWithDifferentNullabilityAndMutability
import com.runninglane.facade.test.cases.`object`.delegate.DelegateWithExactTypes
import com.runninglane.facade.test.cases.`object`.delegate.InnerDelegateWithDifferentNullabilityAndMutability
import com.runninglane.facade.test.cases.`object`.target.InnerTargetWithSimpleValues
import com.runninglane.facade.test.cases.`object`.target.TargetWithObjectValues
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ObjectTest {

    @Test
    fun `should create correct facade from delegate with exact types`() {
        val delegate = DelegateWithExactTypes(
            InnerTargetWithSimpleValues().apply {
                id = 1
                boolean = true
                byte = 1
                short = 2
                char = 'A'
                int = 100
                long = 200L
            }
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithObjectValues::class)

        assertSame(delegate.inner, facade.inner)
    }

    @Test
    fun `should create correct facade from delegate with different nullability and mutability`() {
        val delegate = DelegateWithDifferentNullabilityAndMutability(
            InnerDelegateWithDifferentNullabilityAndMutability(
                id = 1,
                boolean = true,
                byte = 1,
                short = 2,
                char = 'A',
                int = 100,
                long = 200L
            )
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithObjectValues::class)

        assertEquals(facade.inner?.id, delegate.inner.id)
        assertEquals(facade.inner?.boolean, delegate.inner.boolean)
        assertEquals(facade.inner?.byte, delegate.inner.byte)
        assertEquals(facade.inner?.short, delegate.inner.short)
        assertEquals(facade.inner?.char, delegate.inner.char)
        assertEquals(facade.inner?.int, delegate.inner.int)
        assertEquals(facade.inner?.long, delegate.inner.long)
    }

    @Test
    fun `should throw NPE when accessing not-null property but delegate is holding null`() {
        val delegate = DelegateWithDifferentNullabilityAndMutability(
            InnerDelegateWithDifferentNullabilityAndMutability(
                id = 1,
                boolean = true,
                byte = 1,
                short = 2,
                char = 'A',
                int = null,
                long = 200L
            )
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithObjectValues::class)

        assertThrows<NullPointerException> { facade.inner?.int }
    }

}
