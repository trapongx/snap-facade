package com.runninglane.facade

import com.runninglane.facade.bytecode.compile.CompilationSession
import com.runninglane.facade.naming.DefaultNamingStrategy
import com.runninglane.facade.naming.NamingStrategy
import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaMethod

class FacadeClassGenerator(
    val annotationForPropertyInheriting: Set<KClass<Annotation>> = emptySet()
) {
    private val namingStrategy: NamingStrategy = DefaultNamingStrategy()

    fun generate(targetClass: KClass<*>, delegateClass: KClass<*>): KClass<*> {
        val facadeClassName = namingStrategy.buildClassName(targetClass.java, delegateClass.java)
        val facadePackageName = namingStrategy.buildPackageName(targetClass.java, delegateClass.java)
        val src = generateSourceCode(targetClass, delegateClass, facadeClassName, facadePackageName)
        return CompilationSession.compileAndLoad(src, facadeClassName, facadePackageName).kotlin
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

        val typeBuilder = TypeSpec.classBuilder(facadeClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("delegate", delegateClass.asClassName())
                    .build()
            )
            .addProperty(delegatePropertySpec)

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

                    // Check if the delegate property is nullable but target property is not nullable
                    val isDelegateNullable = delegateProperty.returnType.isMarkedNullable
                    val isTargetNullable = targetProperty.returnType.isMarkedNullable

                    val propertyBuilder = PropertySpec.builder(targetProperty.name, returnType)
                        .addModifiers(KModifier.OVERRIDE)
                        .getter(
                            FunSpec.getterBuilder()
                                .addStatement(
                                    if (isDelegateNullable && !isTargetNullable) {
                                        "return delegate.${targetProperty.name}!!"
                                    } else {
                                        "return delegate.${targetProperty.name}"
                                    }
                                )
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
            .addType(typeBuilder.build())

        // Add necessary imports for the file
        return fileBuilder.build().toString()
    }
}