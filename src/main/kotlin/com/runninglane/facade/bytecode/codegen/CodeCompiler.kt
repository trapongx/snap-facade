package com.runninglane.facade.bytecode.codegen

import kotlin.reflect.KClass

interface CodeCompiler {
    /**
     * Finalizes the class definition, generates the bytecode and loads it into the runtime.
     *
     * @param sourceCode
     * @param packageName Target package name for the generated class
     * @param className Name for the generated class
     * @return Generated concrete class
     */
    @JvmSynthetic
    fun compileAndLoadClass(
        sourceCode: String,
        packageName: String,
        className: String
    ): KClass<*>
}