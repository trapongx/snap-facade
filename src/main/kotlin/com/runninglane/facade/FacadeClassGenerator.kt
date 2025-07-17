package com.runninglane.facade

import com.runninglane.facade.bytecode.compile.CompilationSession
import com.runninglane.facade.naming.DefaultNamingStrategy
import com.runninglane.facade.naming.NamingStrategy
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

class FacadeClassGenerator(
    val annotationForPropertyInheriting: Set<KClass<Annotation>> = emptySet()
) {
    private val namingStrategy: NamingStrategy = DefaultNamingStrategy()

    // Simple type packages that we consider as primitive or simple types
    private val simpleTypePackages = setOf(
        "kotlin",
        "java.lang",
        "java.math",
        "java.time",
        "java.util.UUID"
    )

    // Collection type classes that need special handling
    private val collectionTypes = setOf(
        List::class,
        Set::class,
        Map::class
    ).flatMap { listOf(it.qualifiedName, it.java.canonicalName) }.toSet()

    // Mutable collection type classes
    private val mutableCollectionTypes = setOf(
        MutableList::class,
        MutableSet::class,
        MutableMap::class
    ).flatMap { listOf(it.qualifiedName, it.java.canonicalName) }.toSet()

    fun generate(targetClass: KClass<*>, delegateClass: KClass<*>): KClass<*> {
        val facadeClassName = namingStrategy.buildClassName(targetClass.java, delegateClass.java)
        val facadePackageName = namingStrategy.buildPackageName(targetClass.java, delegateClass.java)
        val src = generateSourceCode(targetClass, delegateClass, facadeClassName, facadePackageName)
        return try {
            CompilationSession.compileAndLoad(src, facadeClassName, facadePackageName).kotlin
        } catch (t: Throwable) {
            throw FacadeGenerationException("Error compiling generated source", t)
        }
    }

    /**
     * Determines if a type is considered a simple type (primitive, String, enum, etc.)
     */
    private fun isSimpleType(type: KType): Boolean {
        val typeName = type.classifier.toString()

        // Check if it's in a simple type package
        if (simpleTypePackages.any { typeName.startsWith("class $it.") }) {
            return true
        }

        // Check if it's an enum (safely)
        if (typeName.startsWith("class ")) {
            try {
                val className = typeName.substring(6)
                return Class.forName(className).isEnum
            } catch (_: ClassNotFoundException) {
                // If class not found, it's not a simple type
                return false
            }
        }

        return false
    }

    /**
     * Determines if a type is a collection type
     */
    private fun isCollectionType(type: KType): Boolean {
        val typeName = type.classifier.toString()

        // Check for collection names as plain interfaces/classes (not prefixed with 'class')
        val collectionNames = listOf("List", "Set")
        if (collectionNames.any { typeName.endsWith(it) }) {
            return true
        }

        if (typeName.startsWith("class ") || typeName.startsWith("interface ")) {
            val prefix = if (typeName.startsWith("class ")) "class " else "interface "
            val className = typeName.substring(prefix.length)

            // Direct match with known collection types
            if (className in collectionTypes || className in mutableCollectionTypes) {
                return true
            }

            // Check interfaces safely
            try {
                val clazz = Class.forName(className)
                return clazz.interfaces.any { i -> 
                    i.name in collectionTypes || i.name in mutableCollectionTypes 
                }
            } catch (_: ClassNotFoundException) {
                // If class not found, assume it's not a collection
                return false
            }
        }
        return false
    }

    /**
     * Determines if a type is a specific kind of collection (List, Set, Map, etc.)
     */
    private fun getCollectionTypeCategory(type: KType): String? {
        val typeName = type.classifier.toString()
        if (typeName.startsWith("class ") || typeName.startsWith("interface ")) {
            val prefix = if (typeName.startsWith("class ")) "class " else "interface "
            val className = typeName.substring(prefix.length)

            // Check if it's a List, Set, Map, etc.
            return when {
                className.contains("List") -> "List"
                className.contains("Set") -> "Set"
                else -> null
            }
        }

        return null
    }

    /**
     * Checks if a property's return type string indicates it's a mutable or immutable collection
     */
    private fun isMutableCollectionType(property: kotlin.reflect.KProperty1<*, *>): Boolean {
        val returnTypeStr = property.returnType.toString()

        return returnTypeStr.contains("kotlin.collections.MutableList") ||
                returnTypeStr.contains("kotlin.collections.MutableSet")
    }

    /**
     * Checks if two types are compatible for direct mapping
     */
    private fun areTypesCompatible(targetType: KType, delegateType: KType): Boolean {
        // If they're the same type (ignoring nullability), they're compatible for direct mapping
        return targetType.classifier == delegateType.classifier
    }

    private fun generateSourceCode(
        targetClass: KClass<*>,
        delegateClass: KClass<*>,
        facadeClassName: String,
        facadePackageName: String
    ): String {

        // Create a class that extends or implements the target class
        val delegatePropertySpec = PropertySpec.builder(
            "delegate",
            delegateClass.asClassName(),
            KModifier.PUBLIC
        ).initializer("delegate").build()

        val facadeFactoryPropertySpec = PropertySpec.builder(
            "facadeFactory",
            FacadeFactory::class.asClassName(),
            KModifier.PRIVATE
        ).initializer("facadeFactory").build()

        val typeBuilder = TypeSpec.classBuilder(facadeClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("delegate", delegateClass.asClassName())
                    .addParameter("facadeFactory", FacadeFactory::class)
                    .build()
            )
            .addProperty(delegatePropertySpec)
            .addProperty(facadeFactoryPropertySpec)

        // Make the class extend the target class
        if (targetClass.java.isInterface) {
            typeBuilder.addSuperinterface(targetClass.asClassName())
        } else {
            typeBuilder.superclass(targetClass.asClassName())
        }

        // Process properties of the target class
        val targetProperties = targetClass.memberProperties
        val delegateProperties = delegateClass.memberProperties.associateBy { it.name }

        for (targetProperty in targetProperties) {
            val delegateProperty = delegateProperties[targetProperty.name]

            // Check if the property should be excluded from overriding
            val shouldExclude = targetProperty.annotations.any { it.annotationClass in annotationForPropertyInheriting }

            if (!shouldExclude) {
                val isPropertyMutable = targetProperty is KMutableProperty<*>
                val resolvedPropertyTypeName = resolveTypeName(targetProperty.returnType, emptyMap())

                // Case 1: Property exists in delegate class - override it with delegation
                if (delegateProperty != null) {
                    // Check nullability
                    val isDelegateNullable = delegateProperty.returnType.isMarkedNullable
                    val isTargetNullable = targetProperty.returnType.isMarkedNullable

                    // Check type compatibility
                    val targetType = targetProperty.returnType
                    val delegateType = delegateProperty.returnType
                    val isTargetTypeCollection = isCollectionType(targetType)
                    val isDelegateTypeCollection = isCollectionType(delegateType)
                    val isTargetTypeSimple = !isTargetTypeCollection && isSimpleType(targetType)
                    val isDelegateTypeSimple = !isDelegateTypeCollection && isSimpleType(delegateType)

                    // Generate getter based on property type and nullability
                    val getterBuilder = FunSpec.getterBuilder()

                    // Handle collections that need element type mapping and/or mutability conversion
                    if (isTargetTypeCollection && isDelegateTypeCollection) {
                        val targetCollectionTypeCategory = getCollectionTypeCategory(targetType)
                        val delegateCollectionTypeCategory = getCollectionTypeCategory(delegateType)

                        if (targetCollectionTypeCategory != delegateCollectionTypeCategory) {
                            throw FacadeGenerationException("Cannot create facade for property with different collection types. Property name: ${targetProperty.name}, Target type: $targetCollectionTypeCategory, Delegate type: $delegateCollectionTypeCategory")
                        }

                        // Check if element types are different - use a more reliable approach
                        val hasDifferentElementTypes =
                            targetType.arguments.isNotEmpty() && delegateType.arguments.isNotEmpty() &&
                                    targetType.arguments[0].type?.classifier != delegateType.arguments[0].type?.classifier

                        val isTargetCollectionMutable = isMutableCollectionType(targetProperty)
                        val isDelegateCollectionMutable = isMutableCollectionType(delegateProperty)

                        val nullSafety = if (isDelegateNullable) "?" else ""
                        val returnExpression = buildString {
                            append("return delegate.${targetProperty.name}")
                            var isCurrentExpressionMutable = isDelegateCollectionMutable
                            var currentCollectionTypeCategory = delegateCollectionTypeCategory
                            if (hasDifferentElementTypes) {
                                val resolvedTargetElementType = resolveTypeName(targetType.arguments[0].type!!, emptyMap())
                                append("$nullSafety.map { facadeFactory.from(it).to($resolvedTargetElementType::class) }")
                                isCurrentExpressionMutable = false
                                currentCollectionTypeCategory = "List"
                            }
                            if (isTargetCollectionMutable != isCurrentExpressionMutable
                                || currentCollectionTypeCategory != targetCollectionTypeCategory) {
                                if (isTargetCollectionMutable) {
                                    when (targetCollectionTypeCategory) {
                                        "List" -> append(".toMutableList()")
                                        "Set" -> append(".toMutableSet()")
                                    }
                                } else {
                                    when (targetCollectionTypeCategory) {
                                        "List" -> append(".toList()")
                                        "Set" -> append(".toSet()")
                                    }
                                }
                            }
                        }
                        getterBuilder.addStatement(returnExpression)
                        if (isDelegateNullable && !isTargetNullable) {
                            getterBuilder.addStatement(
                                "?: throw NullPointerException(%S)",
                                "${targetProperty.name} is not-null in $targetClass but null value is held in delegate of type $delegateClass"
                            )
                        }
                    }
                    // Handle non-collection different types that need facade conversion for non-collection types
                    else if (!areTypesCompatible(targetType, delegateType) && !isTargetTypeSimple && !isDelegateTypeSimple) {
                        // Need to handle null and create facade for the property
                        if (isDelegateNullable) {
                            if (isTargetNullable) {
                                getterBuilder.addStatement("return delegate.${targetProperty.name}?.let { facadeFactory.from(it).to() }")
                            } else {
                                getterBuilder.addStatement("return delegate.${targetProperty.name}?.let { facadeFactory.from(it).to() } ?: throw NullPointerException(%S)", "${targetProperty.name} is not-null in $targetClass but null value is held in delegate of type $delegateClass")
                            }
                        } else {
                            getterBuilder.addStatement("return facadeFactory.from(delegate.${targetProperty.name}).to()")
                        }
                    } else {
                        // Direct property access for simple type expecting they are compatible
                        if (isDelegateNullable && !isTargetNullable) {
                            getterBuilder.addStatement(
                                "return delegate.${targetProperty.name} ?: throw NullPointerException(%S)",
                                "${targetProperty.name} is not-null in $targetClass but null value is held in delegate of type $delegateClass"
                            )
                        } else {
                            getterBuilder.addStatement("return delegate.${targetProperty.name}")
                        }
                    }

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

                    typeBuilder.addProperty(propertyBuilder.build())
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

                    typeBuilder.addProperty(propertyBuilder.build())
                }
            }
        }

        // Check for abstract methods in the target class
        val unimplementedMethods = targetClass.memberFunctions.filter { it.isAbstract }.map { it.name }

        // Throw exception if there are unimplemented abstract methods
        if (unimplementedMethods.isNotEmpty()) {
            val errorMessage = StringBuilder("Cannot generate facade class for $targetClass because:\n")
            errorMessage.append("Abstract methods not implemented: ${unimplementedMethods.joinToString(", ")}")

            throw FacadeGenerationException(errorMessage.toString())
        }

        // Build the file
        val fileBuilder = FileSpec.builder(facadePackageName, facadeClassName)
            .addImport("com.runninglane.facade", "from")
            .addType(typeBuilder.build())

        // Add necessary imports for the file
        return fileBuilder.build().toString()
    }

    /**
     * Resolves a KType to a TypeName, handling generic type parameters
     * If the type is a type parameter, it will be resolved using the typeParamsMapByName
     * Also handles complex generic types like List<T> by recursively resolving type arguments
     */
    private fun resolveTypeName(type: KType, typeParamsMapByName: Map<String, KClass<*>>?): TypeName {
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
                        resolveTypeName(argType, typeParamsMapByName)
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