package com.runninglane.facade.property.decorator

import com.runninglane.facade.FacadeGenerationException
import com.runninglane.facade.property.type.CollectionTypeUtils
import com.runninglane.facade.property.type.TypeNameResolver
import com.squareup.kotlinpoet.FunSpec
import kotlin.reflect.KProperty1

internal object CollectionGetterStatementDecorator : GetterStatementDecorator {
    override fun decorate(
        targetProperty: KProperty1<*, *>,
        delegateProperty: KProperty1<*, *>,
        getterBuilder: FunSpec.Builder
    ) {
        val targetType = targetProperty.returnType
        val delegateType = delegateProperty.returnType

        val targetCollectionTypeCategory = CollectionTypeUtils.getCollectionTypeCategory(targetType)
        val delegateCollectionTypeCategory = CollectionTypeUtils.getCollectionTypeCategory(delegateType)

        if (targetCollectionTypeCategory != delegateCollectionTypeCategory) {
            throw FacadeGenerationException(buildString {
                append("Cannot create facade for property with different collection types.")
                append(" Property name: ${targetProperty.name},")
                append(" Target type: $targetCollectionTypeCategory,")
                append(" Delegate type: $delegateCollectionTypeCategory")
            })
        }

        // Check if element types are different - use a more reliable approach
        val targetElementType = targetType.arguments[0].type!!
        val delegateElementType = delegateType.arguments[0].type!!
        val hasDifferentElementTypes = targetElementType.classifier != delegateElementType.classifier

        val isTargetElementNullable = targetElementType.isMarkedNullable
        val isDelegateElementNullable = delegateElementType.isMarkedNullable
        val hasDifferentElementNullability = isTargetElementNullable != isDelegateElementNullable

        val isTargetCollectionMutable = CollectionTypeUtils.isMutableCollectionType(targetProperty)
        val isDelegateCollectionMutable = CollectionTypeUtils.isMutableCollectionType(delegateProperty)

        val isDelegateNullable = delegateType.isMarkedNullable
        val isTargetNullable = targetType.isMarkedNullable

        val nullSafety = if (isDelegateNullable) "?" else ""
        val elementNullSafety = if (isDelegateElementNullable) "?" else ""
        val returnStmtParams = mutableListOf<String>()
        val returnStmt = buildString {
            append("return delegate.${targetProperty.name}")
            var isCurrentExpressionMutable = isDelegateCollectionMutable
            var currentCollectionTypeCategory = delegateCollectionTypeCategory
            if (hasDifferentElementTypes || hasDifferentElementNullability) {
                val resolvedTargetElementType = TypeNameResolver
                    .resolve(targetType.arguments[0].type!!, emptyMap())
                append("$nullSafety.map { element -> element")
                if (hasDifferentElementTypes) {
                    append("$elementNullSafety.let { ")
                    append("facadeFactory.from(element).to($resolvedTargetElementType::class)")
                    append(" }")
                }
                if (isDelegateElementNullable && !isTargetElementNullable) {
                    append(" ?: throw NullPointerException(%S)")
                    returnStmtParams.add("Element type of ${targetProperty.name} is not-null but null value is held in delegate")
                }
                append(" }")
                isCurrentExpressionMutable = false
                currentCollectionTypeCategory = "List"
            }
            if (isTargetCollectionMutable != isCurrentExpressionMutable
                || currentCollectionTypeCategory != targetCollectionTypeCategory) {
                if (isTargetCollectionMutable) {
                    when (targetCollectionTypeCategory) {
                        "List" -> append("$nullSafety.toMutableList()")
                        "Set" -> append("$nullSafety.toMutableSet()")
                    }
                } else {
                    when (targetCollectionTypeCategory) {
                        "List" -> append("$nullSafety.toList()")
                        "Set" -> append("$nullSafety.toSet()")
                    }
                }
            }
            if (isDelegateNullable && !isTargetNullable) {
                append(" ?: throw NullPointerException(%S)")
                returnStmtParams.add("Property ${targetProperty.name} has not-null type but delegate provide null value")
            }
        }
        getterBuilder.addStatement(returnStmt, *returnStmtParams.toTypedArray())
    }
}