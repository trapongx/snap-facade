package com.runninglane.facade

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.*

data class DelegateWithExactTypes(
    var id: Long?,
    var boolean: Boolean?,
    var byte: Byte?,
    var short: Short?,
    var char: Char?,
    var int: Int,
    var long: Long?,
    var float: Float?,
    var double: Double?,
    var string: String?,
    var date: Date?,
    var localDate: LocalDate?,
    var bigInteger: BigInteger?,
    var bigDecimal: BigDecimal?,
    var blob: ByteArray?,
    var clob: String?,
    var enum: TestEnum?,
    var uuid: UUID?,
    var duration: Duration?,
    var instant: Instant?
)