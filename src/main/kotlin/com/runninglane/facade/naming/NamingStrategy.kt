package com.runninglane.facade.naming

interface NamingStrategy {
    fun buildPackageName(targetClass: Class<*>, delegateClass: Class<*>): String

    fun buildClassName(targetClass: Class<*>, delegateClass: Class<*>): String
}
