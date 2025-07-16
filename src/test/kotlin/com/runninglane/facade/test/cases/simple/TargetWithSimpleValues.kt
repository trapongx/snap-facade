package com.runninglane.facade.test.cases.simple

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
    open var id: Long? = null
    open var boolean: Boolean? = null
    open var byte: Byte? = null
    open var short: Short? = null
    open var char: Char? = null
    open var int: Int = 0
    open var long: Long? = null
    open var float: Float? = null
    open var double: Double? = null
    open var string: String? = null
    open var date: Date? = null
    open var localDate: LocalDate? = null
    open var bigInteger: BigInteger? = null
    open var bigDecimal: BigDecimal? = null
    open var blob: ByteArray? = null
    open var clob: String? = null
    open var enum: TestEnum? = null
    open var uuid: UUID? = null
    open var duration: Duration? = null
    open var instant: Instant? = null
}