package com.runninglane.facade.bytecode.codegen

import com.runninglane.facade.exception.FacadeGenerationException
import kotlin.reflect.KClass

/**
 * Implementation of ByteCodeStrategy that uses KotlinPoet to generate source code
 * and then compiles it using the embedded Kotlin compiler.
 */
open class KotlinCodeCompiler : CodeCompiler {

    /**
     * Generates Kotlin source code and compiles it using the CompilationSession
     */
    override fun compileAndLoadClass(
        sourceCode: String,
        packageName: String,
        className: String
    ): KClass<*> {
        try {
            return CompilationSession.compileAndLoad(
                src = sourceCode,
                className = className,
                packageName = packageName
            ).kotlin
        } catch (e: Exception) {
            throw FacadeGenerationException("Failed to compile and load generated class: ${e.message}", e)
        }
    }

}
