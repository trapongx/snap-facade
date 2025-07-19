package com.runninglane.facade.property.decorator

import com.squareup.kotlinpoet.FunSpec
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal interface GetterStatementDecorator {
    fun decorate(
        targetProperty: KProperty1<*, *>,
        targetTypeParams: Map<String, KClass<*>>,
        delegateProperty: KProperty1<*, *>,
        delegateTypeParams: Map<String, KClass<*>>,
        getterBuilder: FunSpec.Builder
    )
}