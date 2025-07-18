package com.runninglane.facade.property.decorator

import com.squareup.kotlinpoet.FunSpec
import kotlin.reflect.KProperty1

internal interface GetterStatementDecorator {
    fun decorate(
        targetProperty: KProperty1<*, *>,
        delegateProperty: KProperty1<*, *>,
        getterBuilder: FunSpec.Builder
    )
}