package com.runninglane.facade.naming

open class DefaultNamingStrategy : NamingStrategy {
    override fun buildPackageName(targetClass: Class<*>, delegateClass: Class<*>): String {
        return targetClass.`package`?.name ?: ""
    }

    override fun buildClassName(targetClass: Class<*>, delegateClass: Class<*>): String {
        fun Class<*>.simpleNameEnclosed() = generateSequence(this) { it.enclosingClass }
            .map { it.simpleName }
            .toList()
            .reversed()
            .joinToString("$")
        return "${targetClass.simpleNameEnclosed()}\$By${delegateClass.simpleNameEnclosed()}"
    }
}