package com.runninglane.facade.bytecode.codegen.tools

import com.runninglane.facade.FacadeClassGenerator
import com.runninglane.facade.bytecode.codegen.CodeCompiler
import com.runninglane.facade.naming.DefaultNamingStrategy
import com.runninglane.facade.naming.NamingStrategy
import java.io.File
import kotlin.reflect.KClass

class StaticKotlinCodeGenerator(
    outputPath: File,
    annotationForPropertyInheriting: Set<KClass<out Annotation>> = emptySet(),
    namingStrategy: NamingStrategy = DefaultNamingStrategy(),
) {
    private val compiler = object : CodeCompiler {
        override fun compileAndLoadClass(
            sourceCode: String,
            packageName: String,
            className: String
        ): KClass<*> {
            // Create source file
            val packagePath = packageName.replace('.', File.separatorChar)
            val packageDir = File(outputPath, packagePath).apply { mkdirs() }
            val sourceFile = File(packageDir, "$className.kt")
            sourceFile.writeText(sourceCode)
            return Any::class
        }
    }

    private val generator = FacadeClassGenerator(
        annotationForPropertyInheriting,
        namingStrategy = namingStrategy,
        compiler = compiler
    )

    fun generate(
        targetClass: KClass<*>,
        targetTypeParams: Map<String, KClass<*>>,
        delegateClass: KClass<*>,
        delegateTypeParams: Map<String, KClass<*>>
    ) = generator.generate(targetClass, targetTypeParams, delegateClass, delegateTypeParams)
}