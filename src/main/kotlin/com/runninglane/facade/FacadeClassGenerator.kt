package com.runninglane.facade

import com.runninglane.facade.bytecode.compile.CompilationSession
import com.runninglane.facade.naming.DefaultNamingStrategy
import com.runninglane.facade.naming.NamingStrategy
import com.runninglane.facade.property.PropertyBuilder
import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

class FacadeClassGenerator(
    val annotationForPropertyInheriting: Set<KClass<Annotation>> = emptySet()
) {
    private val namingStrategy: NamingStrategy = DefaultNamingStrategy()
    private val propertyBuilder: PropertyBuilder = PropertyBuilder(annotationForPropertyInheriting)

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
                // Case 1: Property exists in delegate class - override it with delegation
                propertyBuilder.build(targetClass, targetProperty, delegateClass, delegateProperty)
                    ?.also { propertySpec -> typeBuilder.addProperty(propertySpec) }
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
            .addImport("com.runninglane.facade", "from", "to")
            .addType(typeBuilder.build())

        // Add necessary imports for the file
        return fileBuilder.build().toString()
    }

}