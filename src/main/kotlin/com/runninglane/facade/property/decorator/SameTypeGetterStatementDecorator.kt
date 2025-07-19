package com.runninglane.facade.property.decorator

import com.runninglane.facade.FacadeGenerationException
import com.runninglane.facade.property.type.KTypeUtils
import com.squareup.kotlinpoet.FunSpec
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf

internal object SameTypeGetterStatementDecorator : GetterStatementDecorator {
    override fun decorate(
        targetProperty: KProperty1<*, *>,
        targetTypeParams: Map<String, KClass<*>>,
        delegateProperty: KProperty1<*, *>,
        delegateTypeParams: Map<String, KClass<*>>,
        getterBuilder: FunSpec.Builder
    ) {
        val targetType = KTypeUtils.resolve(targetProperty.returnType.classifier!!, targetTypeParams)
        val delegateType = KTypeUtils.resolve(delegateProperty.returnType.classifier!!, delegateTypeParams)

        if (!delegateType.isSubclassOf(targetType)) {
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