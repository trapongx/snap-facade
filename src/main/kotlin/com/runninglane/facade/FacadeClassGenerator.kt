package com.runninglane.facade

import com.runninglane.facade.bytecode.compile.CompilationSession
import com.runninglane.facade.naming.DefaultNamingStrategy
import com.runninglane.facade.naming.NamingStrategy
import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KType
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

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
        List::class.java.name,
        Set::class.java.name,
        Map::class.java.name,
        Collection::class.java.name,
        Iterable::class.java.name,
        Sequence::class.java.name,
        Array::class.java.name
    )

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
            } catch (e: ClassNotFoundException) {
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
        if (typeName.startsWith("class ")) {
            val className = typeName.substring(6)

            // Direct match with known collection types
            if (className in collectionTypes) {
                return true
            }

            // Check interfaces safely
            try {
                val clazz = Class.forName(className)
                return clazz.interfaces.any { i -> i.name in collectionTypes }
            } catch (e: ClassNotFoundException) {
                // If class not found, assume it's not a collection
                return false
            }
        }
        return false
    }

    /**
     * Checks if two types are compatible for direct mapping
     */
    private fun areTypesCompatible(targetType: KType, delegateType: KType): Boolean {
        // If they're the same type (ignoring nullability), they're compatible for direct mapping
        return targetType.classifier == delegateType.classifier
    }

    /**
     * This function use KotlinPoet to generate a class.
     * This class's package name is facadePackageName, class name is facadeClassName.
     * It has one constructor accepting a parameter of type delegateClass declared as public immutable property named `delegate`.
     * It extends or implements the targetClass.
     * For each of the properties in targetClass, if the property of the same name exist in delegateClass,
     * and if it does not have annotation listed in annotationForPropertyInheriting,
     * the generated class will override the property like this
     *
     * var propertyName: propertyType
     *      get() { delegate.propertyName }
     *      set(_) { error("Making change to propertyName in facade of $targetClass is not allowed") }
     *
     * However, if the property is immutable, just use `val` instead of `var` and omit the setter.
     *
     * For all other properties, if they are concrete, just do nothing, which means just to inherit them as they are.
     * But if any of them are abstract, throw FacadeGenerationException with the message informing about problematic
     * property names. Also, if there's abstract methods, they will not be implemented also,
     * add their name in the list in the error message.
     * 
     * If things go well, build it as source code and return as string.
     */
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

            // Check if property is abstract
            val isAbstract = targetProperty.javaGetter?.modifiers?.let { java.lang.reflect.Modifier.isAbstract(it) } ?: false

            // Check if the property should be excluded from overriding
            val shouldExclude = targetProperty.annotations.any { it.annotationClass in annotationForPropertyInheriting }

            if (!shouldExclude) {
                // Case 1: Property exists in delegate class - override it with delegation
                if (delegateProperty != null) {
                    val isMutable = targetProperty is KMutableProperty<*>
                    val returnType = targetProperty.returnType.asTypeName()

                    // Check nullability
                    val isDelegateNullable = delegateProperty.returnType.isMarkedNullable
                    val isTargetNullable = targetProperty.returnType.isMarkedNullable

                    // Check type compatibility
                    val targetType = targetProperty.returnType
                    val delegateType = delegateProperty.returnType

                    // Generate getter based on property type and nullability
                    val getterBuilder = FunSpec.getterBuilder()

                    // Handle collection types
                    if (isCollectionType(targetType) || isCollectionType(delegateType)) {
                        throw FacadeGenerationException("Delegation of collection type is not yet supported. Property name: ${targetProperty.name}, Target class: $targetClass, Delegate class: $delegateClass")
                    }
                    // Handle simple types
                    else if (isSimpleType(targetType) && isSimpleType(delegateType)) {
                        // For simple values, types must match exactly
                        if (targetType.classifier != delegateType.classifier) {
                            throw FacadeGenerationException("Cannot create facade for property with different simple types. Property name: ${targetProperty.name}, Target class: $targetClass, Delegate class: $delegateClass")
                        }

                        // Handle nullability
                        if (isDelegateNullable && !isTargetNullable) {
                            getterBuilder.addStatement("return delegate.${targetProperty.name} ?: throw NullPointerException(%S)", "${targetProperty.name} is not-null in $targetClass but null value is held in delegate of type $delegateClass")
                        } else {
                            getterBuilder.addStatement("return delegate.${targetProperty.name}")
                        }
                    }
                    // Handle same complex types
                    else if (areTypesCompatible(targetType, delegateType)) {
                        // Same complex type - direct mapping
                        if (isDelegateNullable && !isTargetNullable) {
                            getterBuilder.addStatement("return delegate.${targetProperty.name} ?: throw NullPointerException(%S)", "${targetProperty.name} is not-null in $targetClass but null value is held in delegate of type $delegateClass")
                        } else {
                            getterBuilder.addStatement("return delegate.${targetProperty.name}")
                        }
                    }
                    // Handle different complex types - create facade for them
                    else {
                        // Extract just the simple class name for the reified to() call
                        val targetTypeStr = targetType.classifier.toString()
                        val targetSimpleName = if (targetTypeStr.startsWith("class ")) {
                            val fullClassName = targetTypeStr.substring(6)
                            fullClassName.substringAfterLast('.')
                        } else {
                            targetTypeStr
                        }

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
                    }

                    val propertyBuilder = PropertySpec.builder(targetProperty.name, returnType)
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(getterBuilder.build())

                    if (isMutable) {
                        propertyBuilder.mutable(true)
                            .setter(
                                FunSpec.setterBuilder()
                                    .addParameter("_", returnType)
                                    .addStatement("throw UnsupportedOperationException(%S)", "Making change to ${targetProperty.name} in facade of $targetClass is not allowed")
                                    .build()
                            )
                    }

                    typeBuilder.addProperty(propertyBuilder.build())
                } 
                // Case 2: Property does not exist in delegate class - override it to throw error
                else  {
                    val isMutable = targetProperty is KMutableProperty<*>
                    val returnType = targetProperty.returnType.asTypeName()

                    val propertyBuilder = PropertySpec.builder(targetProperty.name, returnType)
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
                                    .addParameter("_", returnType)
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
            .addImport("kotlin.reflect", "KClass")
            .addImport("com.runninglane.facade", "to", "from")
            .addType(typeBuilder.build())

        // Add necessary imports for the file
        return fileBuilder.build().toString()
    }
}