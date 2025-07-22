package com.runninglane.facade.test.cases.simple

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class DelegateWithDifferentNullabilityAndMutability(
    val id: Long,
    val boolean: Boolean,
    val byte: Byte,
    val short: Short,
    val char: Char,
    val int: Int?,
    val long: Long,
    val float: Float,
    val double: Double,
    val string: String,
    val date: Date,
    val localDate: LocalDate,
    val bigInteger: BigInteger,
    val bigDecimal: BigDecimal,
    val blob: ByteArray,
    val clob: String,
    val enum: TestEnum,
    val uuid: UUID,
    val duration: Duration,
    val instant: Instant
)