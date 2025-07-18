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
            val canConvertWithoutLosingCharacteristics = when (delegateCollectionTypeCategory) {
                "List" -> targetCollectionTypeCategory in listOf("Collection", "Array")
                "Set" -> targetCollectionTypeCategory in listOf("List", "Collection", "Array")
                "Collection" -> targetCollectionTypeCategory in listOf("List", "Array")
                "Array" -> targetCollectionTypeCategory in listOf("List", "Collection")
                else -> false
            }
            if (!canConvertWithoutLosingCharacteristics) {
                throw FacadeGenerationException(buildString {
                    append("Cannot create facade for property with different collection types.")
                    append(" Property name: ${targetProperty.name},")
                    append(" Target type: $targetCollectionTypeCategory,")
                    append(" Delegate type: $delegateCollectionTypeCategory")
                })
            }
        }

        val isTargetCollectionMutable = CollectionTypeUtils.isMutableCollectionType(targetProperty)
        val isDelegateCollectionMutable = CollectionTypeUtils.isMutableCollectionType(delegateProperty)

        val isDelegateNullable = delegateType.isMarkedNullable
        val isTargetNullable = targetType.isMarkedNullable
        val nullSafety = if (isDelegateNullable) "?" else ""

        val returnStmtParams = mutableListOf<String>()
        val returnStmt = buildString {
            append("return delegate.${targetProperty.name}")

            var isCurrentExpressionMutable = isDelegateCollectionMutable
            var currentCollectionTypeCategory = delegateCollectionTypeCategory

            // Check if element types are different - use a more reliable approach
            val hasDifferentElementTypes = targetType.arguments.zip(delegateType.arguments)
                .any { (targetTypeArg, delegateTypeArg) ->
                    val targetElementType = targetTypeArg.type!!
                    val delegateElementType = delegateTypeArg.type!!

                    targetElementType.classifier != delegateElementType.classifier
                            || targetElementType.isMarkedNullable != delegateElementType.isMarkedNullable
                }

            if (hasDifferentElementTypes) {
                fun appendElementValueExpression(index: Int, elementName: String) {
                    append(elementName)

                    val targetElementType = targetType.arguments[index].type!!
                    val delegateElementType = delegateType.arguments[index].type!!
                    val isElementTypeDifferent = targetElementType.classifier != delegateElementType.classifier

                    val isTargetElementNullable = targetElementType.isMarkedNullable
                    val isDelegateElementNullable = delegateElementType.isMarkedNullable
                    val isElementNullabilityDifferent = isTargetElementNullable != isDelegateElementNullable

                    val elementNullSafety = if (isDelegateElementNullable) "?" else ""

                    if (isElementTypeDifferent || isElementNullabilityDifferent) {
                        val resolvedTargetElementType = TypeNameResolver.resolve(targetElementType, emptyMap())

                        if (isElementTypeDifferent) {
                            append("$elementNullSafety.let { ")
                            append("facadeFactory.from($elementName).to(${resolvedTargetElementType.toString().removeSuffix("?")}::class)")
                            appendLine(" }")
                        }

                        if (isDelegateElementNullable && !isTargetElementNullable) {
                            appendLine(" ?: throw NullPointerException(%S)")
                            returnStmtParams.add("Element type of ${targetProperty.name} is not-null but null value is held in delegate")
                        }
                    }
                }

                when (targetCollectionTypeCategory) {
                    "List", "Set", "Collection" -> {
                        appendLine("$nullSafety.map { element ->").let { listOf("element") }
                        appendElementValueExpression(0, "element")
                        append(" }")

                        currentCollectionTypeCategory = "List"
                        isCurrentExpressionMutable = false
                    }

                    "Map" -> {
                        appendLine("$nullSafety.map { (key, value) ->").let { listOf("key", "value") }
                        appendLine("Pair(")
                        appendElementValueExpression(0, "key")
                        appendLine(",")
                        appendElementValueExpression(1, "value")
                        appendLine(")")
                        appendLine(" }$nullSafety.toMap()")

                        currentCollectionTypeCategory = "Map"
                        isCurrentExpressionMutable = false
                    }

                    "Array" -> {
                        appendLine("$nullSafety.map { element ->").let { listOf("element") }
                        appendElementValueExpression(0, "element")
                        append(" }")

                        currentCollectionTypeCategory = "List"
                        isCurrentExpressionMutable = false
                    }

                    else -> error("Should not happen")
                }



            }

            if (isTargetCollectionMutable != isCurrentExpressionMutable
                || currentCollectionTypeCategory != targetCollectionTypeCategory) {
                if (isTargetCollectionMutable) {
                    when (targetCollectionTypeCategory) {
                        "List", "Collection" -> append("$nullSafety.toMutableList()")
                        "Set" -> append("$nullSafety.toMutableSet()")
                        "Map" -> append("$nullSafety.toMutableMap()")
                        "Array" -> append("$nullSafety.toTypedArray()")
                        else -> error("Should not happen")
                    }
                } else {
                    when (targetCollectionTypeCategory) {
                        "List", "Collection" -> append("$nullSafety.toList()")
                        "Set" -> append("$nullSafety.toSet()")
                        "Map" -> append("$nullSafety.toMap()")
                        "Array" -> error("Should not happen")
                        else -> error("Should not happen")
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