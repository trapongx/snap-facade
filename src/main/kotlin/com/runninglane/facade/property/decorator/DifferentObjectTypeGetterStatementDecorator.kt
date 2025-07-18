package com.runninglane.facade.property.decorator

import com.squareup.kotlinpoet.FunSpec
import kotlin.reflect.KProperty1

internal object DifferentObjectTypeGetterStatementDecorator : GetterStatementDecorator {
    override fun decorate(
        targetProperty: KProperty1<*, *>,
        delegateProperty: KProperty1<*, *>,
        getterBuilder: FunSpec.Builder
    ) {
        val isTargetNullable = targetProperty.returnType.isMarkedNullable
        val isDelegateNullable = delegateProperty.returnType.isMarkedNullable

        if (isDelegateNullable) {
            val params = mutableListOf<Any>()
            val statement = buildString {
                append("return delegate.${targetProperty.name}?.let { facadeFactory.from(it).to() }")
                if (!isTargetNullable) {
                    append(" ?: throw NullPointerException(%S)")
                    params.add("${targetProperty.name} has not-null type but delegate provide null")
                }
            }
            getterBuilder.addStatement(statement, *params.toTypedArray())
        } else {
            getterBuilder.addStatement("return facadeFactory.from(delegate.${targetProperty.name}).to()")
        }

    }
}