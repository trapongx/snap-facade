package com.runninglane.facade.bytecode.compile

import org.jetbrains.kotlin.cli.common.arguments.K2JVMCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.config.Services
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.StringWriter
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Files
import javax.tools.*

object CompilationSession {
    val inMemory: Boolean = System.getProperty("snap-facade.compilation.in-memory")?.toBoolean() ?: true

    // Cache of already loaded classes to avoid reloading
    private val loadedClasses = mutableMapOf<String, Class<*>>()

    // Get Java compiler once and reuse
    private val javaCompiler: JavaCompiler by lazy {
        ToolProvider.getSystemJavaCompiler()
            ?: throw kotlin.RuntimeException("Java compiler not available. Make sure you're running with JDK, not JRE.")
    }

    // Keep a global shared directory for all compilations in this session
    private val sessionDir by lazy {
        val dir = Files.createTempDirectory("compilation-session").toFile()
        // Enable cleanup when JVM exits
        dir.deleteOnExit()
        dir
    }

    val sourceDir = File(sessionDir, "src").apply { mkdirs() }
    val outputDir = File(sessionDir, "out").apply { mkdirs() }

    // Use a single shared classloader for all compilations
    private val sharedClassLoader by lazy {
        // Create a classloader with the output directory
        URLClassLoader(arrayOf(outputDir.toURI().toURL()), Thread.currentThread().contextClassLoader)
    }

    // Keep track of all source files created in this session
    private val createdKotlinSources = mutableMapOf<String, File>()
    private val createdJavaSources = mutableMapOf<String, File>()

    /**
     * Compiles and loads a source code string.
     * Uses a persistent session directory to ensure all classes
     * remain available throughout the session.
     *
     * @param src The source code to compile
     * @param className Name of the class to load after compilation
     * @param packageName Package of the class
     * @param language Source language, either "kotlin" or "java"
     * @return The primary loaded class
     */
    fun compileAndLoad(src: String, className: String, packageName: String, language: String = "kotlin"): Class<*> {
        // First load all classes to ensure they're available in the classloader
        val allClasses = when (language.lowercase()) {
            "kotlin" -> when (inMemory) {
                true -> loadAllClasses(src, className, packageName, this::compileKotlinInMemory)
                false -> loadAllClasses(src, className, packageName, this::compileKotlin)
            }
            "java" -> when (inMemory) {
                true -> loadAllClasses(src, className, packageName, this::compileJavaInMemory)
                false -> loadAllClasses(src, className, packageName, this::compileJava)
            }
            else -> throw kotlin.IllegalArgumentException("Unsupported language: $language. Only 'kotlin' and 'java' are supported.")
        }

        // Return the main class
        val fullClassName = "$packageName.$className"
        return allClasses.find { it.name == fullClassName }
            ?: throw kotlin.RuntimeException("Main class $fullClassName not found among compiled classes")
    }

    /**
     * Compiles Kotlin source code and uses the shared classloader
     *
     * @param src The Kotlin source code to compile
     * @param className Name of the class to load after compilation
     * @param packageName Package of the class
     * @return Pair of (ClassLoader, output directory) for loading classes
     */
    private fun compileKotlin(src: String, className: String, packageName: String): Pair<ClassLoader, File> {
        // Get package name - either from parameter or by extracting from source
        val fullClassName = "$packageName.$className"

        // Create source file
        val packagePath = packageName.replace('.', File.separatorChar)
        val packageDir = File(sourceDir, packagePath).apply { mkdirs() }
        val sourceFile = File(packageDir, "$className.kt")
        sourceFile.writeText(src)
        createdKotlinSources[fullClassName] = sourceFile

        // Compile using K2JVMCompiler with all source files
        val compiler = K2JVMCompiler()
        val arguments = K2JVMCompilerArguments().apply {
            freeArgs = createdKotlinSources.values.map { it.absolutePath }
            destination = outputDir.absolutePath
            noStdlib = true
            noReflect = true
            classpath = System.getProperty("java.class.path")
            jvmTarget = System.getProperty("java.version").substringBefore(".")
        }

        val messageCollector = PrintingMessageCollector(
            System.err,
            MessageRenderer.PLAIN_FULL_PATHS,
            false
        )

        val exitCode = compiler.exec(messageCollector, Services.EMPTY, arguments)
        if (exitCode.code != 0) {
            // Get the source file for better error reporting
            val sourceContent = sourceFile.readText()
            throw kotlin.RuntimeException(
                "Kotlin compilation failed with exit code $exitCode.\n" +
                        "Source file: ${sourceFile.absolutePath}\n" +
                        "Content: \n${sourceContent.lines().joinToString("\n")}"
            )
        }

        // Create classloader to load compiled classes
        val classLoader = URLClassLoader(arrayOf(outputDir.toURI().toURL()), Thread.currentThread().contextClassLoader)
        return Pair(classLoader, outputDir)
    }

    /**
     * Recursively collects all classes in a directory and its subdirectories
     */
    private fun collectClassesInDirectory(dir: File, packageName: String, classLoader: ClassLoader, result: MutableList<Class<*>>) {
        dir.listFiles()?.forEach { file ->
            when {
                file.isDirectory -> {
                    // For nested classes in subdirectories
                    val subPackage = if (packageName.isEmpty()) file.name else "$packageName.${file.name}"
                    collectClassesInDirectory(file, subPackage, classLoader, result)
                }
                file.name.endsWith(".class") -> {
                    val className = "$packageName.${file.name.substring(0, file.name.length - 6)}"
                    try {
                        // Check if the class is already loaded
                        val loadedClass = loadedClasses[className] ?: classLoader.loadClass(className).also {
                            loadedClasses[className] = it
                        }

                        // Only add to results if not already in the list
                        if (!result.any { it.name == className }) {
                            result.add(loadedClass)
                        }
                    } catch (e: Exception) {
                        // Log error but continue with other classes
                        println("Warning: Failed to load class $className: ${e.message}")
                    }
                }
            }
        }
    }

    /**
     * Helper method to compile and load all classes using the provided compilation function
     */
    private fun loadAllClasses(
        src: String,
        className: String,
        packageName: String,
        compileFn: (String, String, String) -> Unit
    ): List<Class<*>> {
        compileFn(src, className, packageName)

        // Find all .class files in the output directory that match our package
        val result = mutableListOf<Class<*>>()
        val packagePath = packageName.replace('.', File.separatorChar)
        val packageDir = File(outputDir, packagePath)

        if (packageDir.exists()) {
            collectClassesInDirectory(packageDir, packageName, sharedClassLoader, result)
        }

        return result
    }

    /**
     * Compiles Java source code and sets up a classloader
     *
     * @param src The Java source code to compile
     * @param className Name of the class to load after compilation
     * @param packageName Package of the class
     * @return Pair of (ClassLoader, output directory) for loading classes
     */
    private fun compileJava(src: String, className: String, packageName: String) {
        val fullClassName = "$packageName.$className"

        // Create source file
        val packagePath = packageName.replace('.', File.separatorChar)
        val packageDir = File(sourceDir, packagePath).apply { mkdirs() }
        val sourceFile = File(packageDir, "$className.java")
        sourceFile.writeText(src)
        createdJavaSources[fullClassName] = sourceFile

        // Create a diagnostic collector to capture compilation errors
        val diagnostics = DiagnosticCollector<JavaFileObject>()

        // Get the file manager from the compiler
        val fileManager = javaCompiler.getStandardFileManager(diagnostics, null, null)

        // Create compilation units from the source files
        val compilationUnits = fileManager.getJavaFileObjects(*createdJavaSources.values.toTypedArray())

        // Prepare compilation task with the classpath
        val options = listOf(
            "-d", outputDir.absolutePath,
            "-classpath", System.getProperty("java.class.path")
        )

        // Create a writer for compiler output
        val writer = StringWriter()

        // Execute the compiler task
        val task = javaCompiler.getTask(
            writer,
            fileManager,
            diagnostics,
            options,
            null,
            compilationUnits
        )

        val success = task.call()
        if (!success) {
            val errorMessages = diagnostics.diagnostics.joinToString("\n") { diagnostic ->
                "${diagnostic.source?.name ?: "Unknown source"}: " +
                "line ${diagnostic.lineNumber}, position ${diagnostic.columnNumber}: " +
                diagnostic.getMessage(null)
            }

            val sourceContent = sourceFile.readText()
            throw kotlin.RuntimeException(
                "Java compilation failed:\n$errorMessages\n" +
                        "Source file: ${sourceFile.absolutePath}\n" +
                        "Content:\n${sourceContent.lines().take(20).joinToString("\n")}"
            )
        }

        // Close the file manager
        fileManager.close()
    }

    /**
     * Compiles Kotlin source directly from a string.
     * This uses a temporary directory but cleans up after itself.
     *
     * Note: True in-memory compilation with Kotlin is complex as the compiler
     * is primarily designed for file-based compilation. This implementation
     * uses a dedicated temporary directory for each compilation to minimize
     * file system impact.
     *
     * @param src The Kotlin source code to compile
     * @param className Name of the class to load after compilation
     * @param packageName Package of the class
     * @return Pair of (ClassLoader, output directory) for loading classes
     */
    private fun compileKotlinInMemory(src: String, className: String, packageName: String) {
        // Create source file
        val packagePath = packageName.replace('.', File.separatorChar)
        val packageDir = File(sourceDir, packagePath).apply { mkdirs() }
        val sourceFile = File(packageDir, "$className.kt")
        sourceFile.writeText(src)

        // Compile using K2JVMCompiler
        val compiler = K2JVMCompiler()
        val arguments = K2JVMCompilerArguments().apply {
            freeArgs = listOf(sourceFile.absolutePath)
            destination = outputDir.absolutePath
            noStdlib = true
            noReflect = true
            classpath = System.getProperty("java.class.path")
            jvmTarget = System.getProperty("java.version").substringBefore(".")
        }

        val errorStream = ByteArrayOutputStream()
        val messageCollector = PrintingMessageCollector(
            PrintStream(errorStream),
            MessageRenderer.PLAIN_FULL_PATHS,
            false
        )

        val exitCode = compiler.exec(messageCollector, Services.EMPTY, arguments)
        if (exitCode.code != 0) {
            throw kotlin.RuntimeException(
                "Kotlin compilation failed with exit code $exitCode.\n" +
                        "Compiler output:\n${String(errorStream.toByteArray())}\n" +
                        "Source:\n${src.lines().joinToString("\n")}"
            )
        }
    }

    /**
     * Compiles Java source directly from a string (without creating files)
     * and sets up a classloader.
     *
     * @param src The Java source code to compile
     * @param className Name of the class to load after compilation
     * @param packageName Package of the class
     * @return Pair of (ClassLoader, output directory) for loading classes
     */
    private fun compileJavaInMemory(src: String, className: String, @Suppress("unused") packageName: String) {
        // Create a diagnostic collector to capture compilation errors
        val diagnostics = DiagnosticCollector<JavaFileObject>()

        // Create an in-memory source file
        val sourceObject = object : SimpleJavaFileObject(
            URI.create("string:///$className.java"), JavaFileObject.Kind.SOURCE
        ) {
            override fun getCharContent(ignoreEncodingErrors: Boolean): CharSequence = src
        }

        // Get the file manager from the compiler
        val fileManager = javaCompiler.getStandardFileManager(diagnostics, null, null)

        // Prepare compilation task with the classpath
        val options = listOf(
            "-d", outputDir.absolutePath,
            "-classpath", System.getProperty("java.class.path")
        )

        // Create a writer for compiler output
        val writer = StringWriter()

        // Execute the compiler task
        val task = javaCompiler.getTask(
            writer,
            fileManager,
            diagnostics,
            options,
            null,
            listOf(sourceObject)
        )

        val success = task.call()
        if (!success) {
            val errorMessages = diagnostics.diagnostics.joinToString("\n") { diagnostic ->
                "${diagnostic.source?.name ?: "Unknown source"}: " +
                "line ${diagnostic.lineNumber}, position ${diagnostic.columnNumber}: " +
                diagnostic.getMessage(null)
            }

            throw kotlin.RuntimeException(
                "Java compilation failed:\n$errorMessages\n" +
                        "Source:\n${src.lines().take(20).joinToString("\n")}"
            )
        }

        // Close the file manager
        fileManager.close()
    }

    /**
     * Closes resources associated with this compilation session.
     * Should be called when the session is no longer needed.
     */
    fun dispose() {
        try {
            // For lazy properties, we can check if they've been initialized with this approach
            try {
                // Only close if the classloader has been created
                sharedClassLoader.close()
            } catch (_: UninitializedPropertyAccessException) {
                // Ignore if the property hasn't been accessed yet
            }

            try {
                // Only delete if the directory has been created
                sessionDir.deleteRecursively()
            } catch (_: UninitializedPropertyAccessException) {
                // Ignore if the property hasn't been accessed yet
            }
        } catch (e: Exception) {
            // Log but don't throw, as this is cleanup code
            println("Warning: Error during CompilationSession disposal: ${e.message}")
        }
    }
}