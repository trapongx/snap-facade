package com.runninglane.facade.test.cases.simple

import com.runninglane.facade.FacadeFactory
import com.runninglane.facade.from
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.*
import kotlin.test.assertEquals

class SimpleTest {
    data class DelegateAsInnerInterface(
        val id: Long,
        var int: Int?,
        var long: Long,
    )

    interface InnerDepth1 {
        data class DelegateAsInnerOfInnerInterface(
            val id: Long,
            var int: Int?,
            var long: Long
        )
    }

    @Test
    fun `should create correct facade from delegate with exact types`() {
        val delegate = DelegateWithExactTypes(
            id = 1,
            boolean = true,
            byte = 1,
            short = 2,
            char = 'A',
            int = 100,
            long = 200L,
            float = 1.5f,
            double = 2.5,
            string = "Test1",
            date = Date(),
            localDate = LocalDate.now(),
            bigInteger = BigInteger.valueOf(300),
            bigDecimal = BigDecimal("400.5"),
            blob = byteArrayOf(1, 2, 3),
            clob = "Test CLOB 1",
            enum = TestEnum.ONE,
            uuid = UUID.randomUUID(),
            duration = Duration.ofHours(1),
            instant = Instant.now()
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithSimpleValues::class)

        assertEquals(delegate.id, facade.id)
        assertEquals(facade.boolean, delegate.boolean)
        assertEquals(facade.byte, delegate.byte)
        assertEquals(facade.short, delegate.short)
        assertEquals(facade.char, delegate.char)
        assertEquals(facade.int, delegate.int)
        assertEquals(facade.long, delegate.long)
        assertEquals(facade.float, delegate.float)
        assertEquals(facade.double, delegate.double)
        assertEquals(facade.string, delegate.string)
        assertEquals(facade.date?.time, delegate.date?.time)
        assertEquals(facade.localDate, delegate.localDate)
        assertEquals(facade.bigInteger, delegate.bigInteger)
        assertEquals(facade.bigDecimal?.stripTrailingZeros(), delegate.bigDecimal?.stripTrailingZeros())
        assertEquals(facade.blob, delegate.blob)
        assertEquals(facade.clob, delegate.clob)
        assertEquals(facade.enum, delegate.enum)
        assertEquals(facade.uuid, delegate.uuid)
        assertEquals(facade.duration, delegate.duration)
        assertEquals(facade.instant, delegate.instant)
    }

    @Test
    fun `should create correct facade from delegate with different nullability and fail when accessing unprojected properties`() {
        val delegate = DelegateWithDifferentNullability(
            id = 1,
            int = 100,
            long = 200L,
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithSimpleValues::class)

        assertEquals(facade.id, delegate.id)
        assertEquals(facade.int, delegate.int)
        assertEquals(facade.long, delegate.long)
        assertEquals(facade.long, delegate.long)
        assertThrows<UnsupportedOperationException> { facade.boolean }
        assertThrows<UnsupportedOperationException> { facade.byte }
        assertThrows<UnsupportedOperationException> { facade.short }
        assertThrows<UnsupportedOperationException> { facade.char }
        assertThrows<UnsupportedOperationException> { facade.float }
        assertThrows<UnsupportedOperationException> { facade.double }
        assertThrows<UnsupportedOperationException> { facade.string }
        assertThrows<UnsupportedOperationException> { facade.date }
        assertThrows<UnsupportedOperationException> { facade.localDate }
        assertThrows<UnsupportedOperationException> { facade.bigInteger }
        assertThrows<UnsupportedOperationException> { facade.bigDecimal }
        assertThrows<UnsupportedOperationException> { facade.blob }
        assertThrows<UnsupportedOperationException> { facade.clob }
        assertThrows<UnsupportedOperationException> { facade.enum }
        assertThrows<UnsupportedOperationException> { facade.uuid }
        assertThrows<UnsupportedOperationException> { facade.duration }
        assertThrows<UnsupportedOperationException> { facade.instant }
    }

    @Test
    fun `should create correct facade from delegate with inner depth 1`() {
        val delegate = DelegateAsInnerInterface(
            id = 1,
            int = 100,
            long = 200L,
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithSimpleValues::class)

        assertEquals(facade.id, delegate.id)
        assertEquals(facade.int, delegate.int)
        assertEquals(facade.long, delegate.long)
    }

    @Test
    fun `should create correct facade from delegate with inner depth 2`() {
        val delegate = InnerDepth1.DelegateAsInnerOfInnerInterface(
            id = 1,
            int = 100,
            long = 200L,
        )
        val facade = FacadeFactory.Companion.default.from(delegate).to(TargetWithSimpleValues::class)

        assertEquals(facade.id, delegate.id)
        assertEquals(facade.int, delegate.int)
        assertEquals(facade.long, delegate.long)
    }
}
