package com.runninglane.facade.property.decorator.collection

import kotlin.reflect.KType

internal interface Converter {
    /**
     * @param from, the type of original
     * @param fromStr, the string representation of `from`
     * @param to, the target type after conversion
     * @param toStr, the string representation of `to`
     * @return converter function string, or null if this rule does not apply
     */
    fun convert(from: KType, fromStr: String, to: KType, toStr: String): String?
}