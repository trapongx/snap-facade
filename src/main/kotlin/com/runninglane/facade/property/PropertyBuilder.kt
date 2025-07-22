package com.runninglane.facade.property

import com.runninglane.facade.property.decorator.CollectionGetterStatementDecorator
import com.runninglane.facade.property.decorator.DifferentObjectTypeGetterStatementDecorator
import com.runninglane.facade.property.decorator.SameTypeGetterStatementDecorator
import com.runninglane.facade.property.type.CollectionTypeUtils
import com.runninglane.facade.property.type.KTypeUtils
import com.runninglane.facade.property.type.SimpleTypeUtils
import com.runninglane.facade.property.type.TypeNameResolver
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.jvmErasure

internal class PropertyBuilder(
    val annotationForPropertyInheriting: Set<KClass<out Annotation>>
) {
    fun build(
        targetClass: KClass<*>,
        targetTypeParams: Map<String, KClass<*>>,
        targetProperty: KProperty1<*, *>,
        delegateClass: KClass<*>,
        delegateTypeParams: Map<String, KClass<*>>,
        delegateProperty: KProperty1<*, *>?
    ): PropertySpec? {
        // Check if the property should be excluded from overriding
        val shouldExclude = targetProperty.annotations.any { it.annotationClass in annotationForPropertyInheriting }

        if (shouldExclude)
            return null

        val isPropertyMutable = targetProperty is KMutableProperty<*>

        val resolvedTargetPropertyTypeName = TypeNameResolver.resolve(targetProperty.returnType, targetTypeParams)

        // Case 1: Property exists in delegate class - override it with delegation
        if (delegateProperty != null) {

            val resolvedDelegatePropertyTypeName = TypeNameResolver.resolve(delegateProperty.returnType, delegateTypeParams)

            // Check type compatibility
            val targetPropertyType = targetProperty.returnType.classifier
                ?.let { KTypeUtils.resolve(it, targetTypeParams) }
                ?: targetProperty.returnType.jvmErasure

            // Generate getter based on property type and nullability
            val getterBuilder = FunSpec.getterBuilder()

            val getterStatementDecorator = when {
                CollectionTypeUtils.isCollectionType(targetPropertyType)
                    -> CollectionGetterStatementDecorator

                !SimpleTypeUtils.isSimpleType(targetPropertyType) &&
                        !areTypesCompatible(resolvedTargetPropertyTypeName, resolvedDelegatePropertyTypeName)
                    -> DifferentObjectTypeGetterStatementDecorator

                else -> SameTypeGetterStatementDecorator
            }

            getterStatementDecorator.decorate(
                targetProperty,
                targetTypeParams,
                delegateProperty,
                delegateTypeParams,
                getterBuilder
            )

            val propertyBuilder = PropertySpec.builder(targetProperty.name, resolvedTargetPropertyTypeName)
                .addModifiers(KModifier.OVERRIDE)
                .getter(getterBuilder.build())

            if (isPropertyMutable) {
                propertyBuilder.mutable(true)
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter("_", resolvedTargetPropertyTypeName)
                            .addStatement("throw UnsupportedOperationException(%S)", "Making change to ${targetProperty.name} in facade of $targetClass is not allowed")
                            .build()
                    )
            }

            return propertyBuilder.build()
        }
        // Case 2: Property does not exist in delegate class - override it to throw error
        else  {
            val propertyBuilder = PropertySpec.builder(targetProperty.name, resolvedTargetPropertyTypeName)
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement("throw UnsupportedOperationException(%S)", "Property `${targetProperty.name}` does not exist in delegate class $delegateClass")
                        .build()
                )

            if (isPropertyMutable) {
                propertyBuilder.mutable(true)
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter("_", resolvedTargetPropertyTypeName)
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
    private fun areTypesCompatible(targetTypeName: TypeName, delegateTypeName: TypeName): Boolean {
        // If they're the same type (ignoring nullability), they're compatible for direct mapping
        return targetTypeName.copy(nullable = false) == delegateTypeName.copy(nullable = false)
    }
}