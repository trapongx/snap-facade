package com.runninglane.facade.util

import com.runninglane.facade.annotation.Facade

object FacadeUtils {
    fun getAnnotation(facade: Any): Facade? {
        return facade::class.java.getAnnotation(Facade::class.java)
    }
}