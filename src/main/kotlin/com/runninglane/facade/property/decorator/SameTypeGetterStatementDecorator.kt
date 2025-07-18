package com.runninglane.facade.property.decorator

import com.runninglane.facade.FacadeGenerationException
import com.squareup.kotlinpoet.FunSpec
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.jvm.jvmErasure

internal object SameTypeGetterStatementDecorator : GetterStatementDecorator {
    override fun decorate(
        targetProperty: KProperty1<*, *>,
        delegateProperty: KProperty1<*, *>,
        getterBuilder: FunSpec.Builder
    ) {
        val targetType = targetProperty.returnType
        val delegateType = delegateProperty.returnType

        if (!delegateType.jvmErasure.isSubclassOf(targetType.jvmErasure)) {
            throw FacadeGenerationException("Cannot create facade property of type $targetType from $delegateType")
        }

        val isTargetNullable = targetProperty.returnType.isMarkedNullable
        val isDelegateNullable = delegateProperty.returnType.isMarkedNullable

        val params = mutableListOf<Any>()
        val statement = buildString {
            append("return delegate.${targetProperty.name}")
            if (isDelegateNullable && !isTargetNullable) {
                append(" ?: throw NullPointerException(%S)")
                params.add("Property ${targetProperty.name} has not-null type but delegate provide null value")
            }
        }
        getterBuilder.addStatement(statement, *params.toTypedArray())
    }
}