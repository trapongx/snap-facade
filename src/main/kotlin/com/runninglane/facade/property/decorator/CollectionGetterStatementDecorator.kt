package com.runninglane.facade.property.decorator

import com.runninglane.facade.property.decorator.collection.CollectionConverter
import com.runninglane.facade.property.decorator.collection.MapConverter
import com.runninglane.facade.property.type.CollectionTypeUtils
import com.runninglane.facade.property.type.TypeNameResolver
import com.squareup.kotlinpoet.FunSpec
import kotlin.reflect.KProperty1
import kotlin.reflect.full.createType

internal object CollectionGetterStatementDecorator : GetterStatementDecorator {

    val converters = listOf(CollectionConverter, MapConverter)

    override fun decorate(
        targetProperty: KProperty1<*, *>,
        delegateProperty: KProperty1<*, *>,
        getterBuilder: FunSpec.Builder
    ) {
        val targetType = targetProperty.returnType
        val delegateType = delegateProperty.returnType

        val targetCollectionTypeCategory = CollectionTypeUtils.getCollectionTypeCategory(targetType)

        val isDelegateNullable = delegateType.isMarkedNullable
        val isTargetNullable = targetType.isMarkedNullable
        val nullSafety = if (isDelegateNullable) "?" else ""

        val returnStmtParams = mutableListOf<String>()
        val returnStmt = buildString {
            append("return delegate.${targetProperty.name}")

            var currentExpressionType = delegateType

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

                if (targetCollectionTypeCategory == "Map") {
                    appendLine("$nullSafety.map { (key, value) ->").let { listOf("key", "value") }
                    appendLine("Pair(")
                    appendElementValueExpression(0, "key")
                    appendLine(",")
                    appendElementValueExpression(1, "value")
                    appendLine(")")
                    appendLine(" }$nullSafety.toMap()")

                    currentExpressionType = Map::class.createType(arguments = delegateType.arguments)
                } else {
                    appendLine("$nullSafety.map { element ->").let { listOf("element") }
                    appendElementValueExpression(0, "element")
                    append(" }")

                    currentExpressionType = List::class.createType(arguments = delegateType.arguments)
                }
            }

            val targetTypeName by lazy { targetType.toString().substringBefore("<") }
            val currentExpressionTypeName = currentExpressionType.toString().substringBefore("<")
            if (currentExpressionTypeName != targetTypeName) {

                val (conversionExpression, params) = converters.firstNotNullOfOrNull { converter ->
                    converter.convert(currentExpressionType, currentExpressionTypeName, targetType, targetTypeName)
                } ?: error("Conversion rule not found for $currentExpressionTypeName -> $targetTypeName")

                if (conversionExpression.isNotEmpty()) {
                    appendLine("$nullSafety.$conversionExpression")
                    returnStmtParams.addAll(params)
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