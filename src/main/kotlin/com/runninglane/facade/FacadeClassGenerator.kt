package com.runninglane.facade

import com.runninglane.facade.bytecode.compile.CompilationSession
import com.runninglane.facade.naming.DefaultNamingStrategy
import com.runninglane.facade.naming.NamingStrategy
import com.runninglane.facade.property.PropertyBuilder
import com.runninglane.facade.property.type.TypeNameResolver
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

private typealias FacadeAnnotation = com.runninglane.facade.annotation.Facade

class FacadeClassGenerator(
    val annotationForPropertyInheriting: Set<KClass<Annotation>> = emptySet()
) {
    private val namingStrategy: NamingStrategy = DefaultNamingStrategy()
    private val propertyBuilder: PropertyBuilder = PropertyBuilder(annotationForPropertyInheriting)

    fun generate(
        targetClass: KClass<*>,
        targetTypeParams: Map<String, KClass<*>>,
        delegateClass: KClass<*>,
        delegateTypeParams: Map<String, KClass<*>>
    ): KClass<*> {
        return try {
            val facadeClassName = namingStrategy.buildClassName(targetClass.java, delegateClass.java)
            val facadePackageName = namingStrategy.buildPackageName(targetClass.java, delegateClass.java)
            val src = generateSourceCode(
                targetClass,
                targetTypeParams,
                delegateClass,
                delegateTypeParams,
                facadeClassName,
                facadePackageName
            )
            CompilationSession.compileAndLoad(src, facadeClassName, facadePackageName).kotlin
        } catch (t: Throwable) {
            throw FacadeGenerationException("Error compiling generated source", t)
        }
    }

    private fun generateSourceCode(
        targetClass: KClass<*>,
        targetTypeParams: Map<String, KClass<*>>,
        delegateClass: KClass<*>,
        delegateTypeParams: Map<String, KClass<*>>,
        facadeClassName: String,
        facadePackageName: String
    ): String {

        val resolvedDelegateTypeName = TypeNameResolver.resolve(delegateClass, delegateTypeParams)
        val resolvedTargetTypeName = TypeNameResolver.resolve(targetClass, targetTypeParams)

        // Create a class that extends or implements the target class
        val delegatePropertySpec = PropertySpec.builder(
            "delegate",
            resolvedDelegateTypeName,
            KModifier.PUBLIC, KModifier.OVERRIDE
        ).initializer("delegate").build()

        val facadeFactoryPropertySpec = PropertySpec.builder(
            "facadeFactory",
            FacadeFactory::class.asClassName(),
            KModifier.PRIVATE
        ).initializer("facadeFactory").build()

        val typeBuilder = TypeSpec.classBuilder(facadeClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("delegate", resolvedDelegateTypeName)
                    .addParameter("facadeFactory", FacadeFactory::class)
                    .build()
            )
            .addProperty(delegatePropertySpec)
            .addProperty(facadeFactoryPropertySpec)
            .addSuperinterface(
                Facade::class.asClassName()
                    .parameterizedBy(resolvedDelegateTypeName)
            )
            .addAnnotation(AnnotationSpec.builder(FacadeAnnotation::class)
                .addMember("delegateClass = %T::class", delegateClass.java)
                .addMember("targetClass = %T::class", targetClass.java)
                .build())

        // Make the class extend the target class
        if (targetClass.java.isInterface) {
            typeBuilder.addSuperinterface(resolvedTargetTypeName)
        } else {
            typeBuilder.superclass(resolvedTargetTypeName)
        }

        // Process properties of the target class
        val targetProperties = targetClass.memberProperties
            .filter { !it.isFinal && it.visibility in listOf(KVisibility.PUBLIC, KVisibility.PROTECTED) }
        val delegateProperties = delegateClass.memberProperties
            .filter { it.visibility == KVisibility.PUBLIC }
            .associateBy { it.name }

        for (targetProperty in targetProperties) {
            val delegateProperty = delegateProperties[targetProperty.name]

            // Check if the property should be excluded from overriding
            val shouldExclude = targetProperty.annotations.any { it.annotationClass in annotationForPropertyInheriting }

            if (!shouldExclude) {
                // Case 1: Property exists in delegate class - override it with delegation
                propertyBuilder.build(
                    targetClass,
                    targetTypeParams,
                    targetProperty,
                    delegateClass,
                    delegateTypeParams,
                    delegateProperty
                )?.also { propertySpec -> typeBuilder.addProperty(propertySpec) }
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