package com.runninglane.facade

/**
 * Exception thrown when a facade class cannot be generated.
 */
class FacadeGenerationException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
