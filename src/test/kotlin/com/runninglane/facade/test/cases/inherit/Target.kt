package com.runninglane.facade.test.cases.inherit

open class Target {
    open var id: Long? = null
    open var boolean: Boolean? = null
    @PropertyToInherit
    open val greeting: String?
        get() = "Hello"
}