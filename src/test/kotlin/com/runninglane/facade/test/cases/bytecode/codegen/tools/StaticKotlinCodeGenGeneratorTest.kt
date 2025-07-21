package com.runninglane.facade.test.cases.bytecode.codegen.tools

import com.runninglane.facade.bytecode.codegen.tools.StaticKotlinCodeGenerator
import org.junit.jupiter.api.Test
import java.nio.file.Files
import kotlin.test.assertEquals

class StaticKotlinCodeGenGeneratorTest {
    data class Delegate(
        var name: String
    )

    open class Target {
        open var name: String? = null
    }

    @Test
    fun testGenerateStaticCode() {
        val outputPath = Files.createTempDirectory("static-kotlin-codegen-test")
        try {
            val tool = StaticKotlinCodeGenerator(outputPath.toFile())

            tool.generate(
                Target::class,
                emptyMap(),
                Delegate::class,
                emptyMap()
            )

            val allFilesGenerated = Files.walk(outputPath)
                .filter { Files.isRegularFile(it) }
                .map { it.toString().removePrefix("$outputPath/") }
                .sorted()
                .toList()

            assertEquals(1, allFilesGenerated.size)
            assertEquals(
                listOf(
                    "com/runninglane/facade/test/cases/bytecode/codegen/tools/StaticKotlinCodeGenGeneratorTest\$Target\$ByStaticKotlinCodeGenGeneratorTest\$Delegate.kt"
                ),
                allFilesGenerated
            )
        } finally {
            outputPath.toFile().deleteRecursively()
        }
    }
}