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

    val delegateWithExactTypes = DelegateWithExactTypes(
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

    @Test
    fun `should create facade that map simple properties without further wrapping`() {
        val delegate = delegateWithExactTypes
        val facade = FacadeFactory.default.from(delegateWithExactTypes).to(TargetWithSimpleValues::class)

        assertEquals(delegate::id.returnType, facade::id.returnType)
        assertEquals(delegate::boolean.returnType, facade::boolean.returnType)
        assertEquals(delegate::byte.returnType, facade::byte.returnType)
        assertEquals(delegate::short.returnType, facade::short.returnType)
        assertEquals(delegate::char.returnType, facade::char.returnType)
        assertEquals(delegate::int.returnType, facade::int.returnType)
        assertEquals(delegate::long.returnType, facade::long.returnType)
        assertEquals(delegate::float.returnType, facade::float.returnType)
        assertEquals(delegate::double.returnType, facade::double.returnType)
        assertEquals(delegate::string.returnType, facade::string.returnType)
        assertEquals(delegate::date.returnType, facade::date.returnType)
        assertEquals(delegate::localDate.returnType, facade::localDate.returnType)
        assertEquals(delegate::bigInteger.returnType, facade::bigInteger.returnType)
        assertEquals(delegate::bigDecimal.returnType, facade::bigDecimal.returnType)
        assertEquals(delegate::blob.returnType, facade::blob.returnType)
        assertEquals(delegate::clob.returnType, facade::clob.returnType)
        assertEquals(delegate::enum.returnType, facade::enum.returnType)
        assertEquals(delegate::uuid.returnType, facade::uuid.returnType)
        assertEquals(delegate::duration.returnType, facade::duration.returnType)
        assertEquals(delegate::instant.returnType, facade::instant.returnType)
    }

    @Test
    fun `should create correct facade from delegate with exact types`() {
        val delegate = delegateWithExactTypes
        val facade = FacadeFactory.default.from(delegate).to(TargetWithSimpleValues::class)

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
    fun `should create correct facade from delegate with different nullability and mutability`() {
        val delegate = DelegateWithDifferentNullabilityAndMutability(
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
        val facade = FacadeFactory.default.from(delegate).to(TargetWithSimpleValues::class)

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
        assertEquals(facade.date?.time, delegate.date.time)
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
    fun `should fail when accessing unprojected properties`() {
        val delegate = DelegateWithSubsetOfProperties(
            id = 1,
            int = 100,
            long = 200L
        )
        val facade = FacadeFactory.default.from(delegate).to(TargetWithSimpleValues::class)

        assertEquals(facade.id, delegate.id)
        assertEquals(facade.int, delegate.int)
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
        val facade = FacadeFactory.default.from(delegate).to(TargetWithSimpleValues::class)

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
        val facade = FacadeFactory.default.from(delegate).to(TargetWithSimpleValues::class)

        assertEquals(facade.id, delegate.id)
        assertEquals(facade.int, delegate.int)
        assertEquals(facade.long, delegate.long)
    }
}
