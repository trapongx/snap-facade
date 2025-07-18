package com.runninglane.facade.property

import com.runninglane.facade.property.decorator.CollectionGetterStatementDecorator
import com.runninglane.facade.property.decorator.DifferentObjectTypeGetterStatementDecorator
import com.runninglane.facade.property.decorator.SameTypeGetterStatementDecorator
import com.runninglane.facade.property.type.CollectionTypeUtils
import com.runninglane.facade.property.type.SimpleTypeUtils
import com.runninglane.facade.property.type.TypeNameResolver
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

internal class PropertyBuilder(
    val annotationForPropertyInheriting: Set<KClass<Annotation>>
) {
    fun build(
        targetClass: KClass<*>,
        targetProperty: KProperty1<*, *>,
        delegateClass: KClass<*>,
        delegateProperty: KProperty1<*, *>?
    ): PropertySpec? {
        // Check if the property should be excluded from overriding
        val shouldExclude = targetProperty.annotations.any { it.annotationClass in annotationForPropertyInheriting }

        if (shouldExclude)
            return null

        val isPropertyMutable = targetProperty is KMutableProperty<*>
        val resolvedPropertyTypeName = TypeNameResolver.resolve(targetProperty.returnType, emptyMap())


        // Case 1: Property exists in delegate class - override it with delegation
        if (delegateProperty != null) {
            // Check type compatibility
            val targetType = targetProperty.returnType
            val delegateType = delegateProperty.returnType

            // Generate getter based on property type and nullability
            val getterBuilder = FunSpec.getterBuilder()

            val getterStatementDecorator = when {
                CollectionTypeUtils.isCollectionType(targetType)
                    -> CollectionGetterStatementDecorator

                !SimpleTypeUtils.isSimpleType(targetType) && !areTypesCompatible(targetType, delegateType)
                    -> DifferentObjectTypeGetterStatementDecorator

                else -> SameTypeGetterStatementDecorator
            }

            getterStatementDecorator.decorate(targetProperty, delegateProperty, getterBuilder)

            val propertyBuilder = PropertySpec.builder(targetProperty.name, resolvedPropertyTypeName)
                .addModifiers(KModifier.OVERRIDE)
                .getter(getterBuilder.build())

            if (isPropertyMutable) {
                propertyBuilder.mutable(true)
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter("_", resolvedPropertyTypeName)
                            .addStatement("throw UnsupportedOperationException(%S)", "Making change to ${targetProperty.name} in facade of $targetClass is not allowed")
                            .build()
                    )
            }

            return propertyBuilder.build()
        }
        // Case 2: Property does not exist in delegate class - override it to throw error
        else  {
            val isMutable = targetProperty is KMutableProperty<*>

            val propertyBuilder = PropertySpec.builder(targetProperty.name, resolvedPropertyTypeName)
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement("throw UnsupportedOperationException(%S)", "Property `${targetProperty.name}` does not exist in delegate class $delegateClass")
                        .build()
                )

            if (isMutable) {
                propertyBuilder.mutable(true)
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter("_", resolvedPropertyTypeName)
                            .addStatement("throw UnsupportedOperationException(%S)", "Making change to ${targetProperty.name} in facade of $targetClass is not allowed")
                            .build()
                    )
            }

            return propertyBuilder.build()
        }
    }

    /**
     * Checks if two types are compatible for direct mapping
     */
    private fun areTypesCompatible(targetType: KType, delegateType: KType): Boolean {
        // If they're the same type (ignoring nullability), they're compatible for direct mapping
        return targetType.classifier == delegateType.classifier
    }
}