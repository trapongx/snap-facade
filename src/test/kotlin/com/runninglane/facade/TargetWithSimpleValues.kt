package com.runninglane.facade

import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.util.*

enum class TestEnum {
    ONE, TWO
}

open class TargetWithSimpleValues {
    var id: Long? = null
    var boolean: Boolean? = null
    var byte: Byte? = null
    var short: Short? = null
    var char: Char? = null
    var int: Int = 0
    var long: Long? = null
    var float: Float? = null
    var double: Double? = null
    var string: String? = null
    var date: Date? = null
    var localDate: LocalDate? = null
    var bigInteger: BigInteger? = null
    var bigDecimal: BigDecimal? = null
    var blob: ByteArray? = null
    var clob: String? = null
    var enum: TestEnum? = null
    var uuid: UUID? = null
    var duration: Duration? = null
    var instant: Instant? = null
}