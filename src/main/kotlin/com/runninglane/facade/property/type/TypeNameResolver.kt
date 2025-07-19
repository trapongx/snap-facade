package com.runninglane.facade.property.type

import com.runninglane.facade.FacadeGenerationException
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

object TypeNameResolver {

    fun resolve(`class`: KClass<*>, typeParamsMapByName: Map<String, KClass<*>>?) =
        resolve(
            `class`.createType(
                `class`.typeParameters.map {
                    KTypeProjection.invariant(
                        typeParamsMapByName?.get(it.name)?.createType()
                            ?: throw FacadeGenerationException("Type parameter ${it.name} is not mapped to a target type. Please provide a mapping for it.")
                    )
                }
            ),
            typeParamsMapByName
        )

    /**
     * Resolves a KType to a TypeName, handling generic type parameters
     * If the type is a type parameter, it will be resolved using the typeParamsMapByName
     * Also handles complex generic types like List<T> by recursively resolving type arguments
     */
    fun resolve(type: KType, typeParamsMapByName: Map<String, KClass<*>>?): TypeName {
        // Debug information to help track type resolution
        // println("Resolving type: $type, classifier: ${type.classifier}, jvmErasure: ${type.jvmErasure}")
        val classifier = type.classifier
        // Get the full original type string to preserve exact type information
        val fullTypeStr = type.toString()
        // Extract just the class name part (before any generic parameters)
        val exactTypeStr = fullTypeStr.substringBefore('<')

        return when {
            // Case 1: Direct type parameter (e.g., T)
            classifier is KTypeParameter -> {
                val paramName = classifier.name
                val concreteType = typeParamsMapByName?.get(paramName)
                    ?: throw FacadeGenerationException(">$classifier< is not mapped to a target type. Please provide a mapping for it.")

                // Create a TypeName from the concrete type, preserving nullability
                concreteType.asTypeName().considerJavaNullability(type)
            }

            // Case 2: Generic class with type arguments (e.g., List<T>)
            classifier is KClass<*> && type.arguments.isNotEmpty()-> {
                // Use a more precise approach to get the exact type class name
                // This preserves properties like mutability that might be lost in classifier
                val rawTypeName = if (exactTypeStr != classifier.qualifiedName) {
                    // The type string doesn't match the classifier's qualified name
                    // This indicates we need to use the exact type from the string
                    val packageName = exactTypeStr.substringBeforeLast('.', "").takeIf { it.isNotEmpty() } ?: ""
                    val simpleClassName = exactTypeStr.substringAfterLast('.')

                    // Only create a custom ClassName if we have valid package and class names
                    if (packageName.isNotEmpty() && simpleClassName.isNotEmpty()) {
                        ClassName(packageName, simpleClassName)
                    } else {
                        classifier.asClassName()
                    }
                } else {
                    // Type string matches classifier's qualified name, so just use the classifier directly
                    classifier.asClassName()
                }

                // Process each type argument
                val typeArguments = type.arguments.map { projection ->
                    val argType = projection.type
                    if (argType == null) {
                        // Handle star projection (*)
                        STAR
                    } else {
                        // Recursively resolve the type argument
                        resolve(argType, typeParamsMapByName)
                    }
                }

                // Create a parameterized type name
                rawTypeName.parameterizedBy(typeArguments).considerJavaNullability(type)
            }

            // For regular types without type arguments, just use the standard asTypeName
            else -> type.asTypeName().considerJavaNullability(type)
        }
    }

    private fun TypeName.considerJavaNullability(type: KType): TypeName = when {
        type.isMarkedNullable || type.toString().endsWith("!") -> copy(nullable = true)
        else -> this
    }
}