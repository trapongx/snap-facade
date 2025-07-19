package com.runninglane.facade.property.type

import kotlin.reflect.KClass
import kotlin.reflect.KClassifier
import kotlin.reflect.KTypeParameter

object KTypeUtils {
    fun resolve(classifier: KClassifier, typeParams: Map<String, KClass<*>>): KClass<*> {
        return when (classifier) {
            is KClass<*> -> classifier
            is KTypeParameter -> typeParams[classifier.name] ?: error("Type parameter ${classifier.name} not provided in map")
            else -> error("Unsupported classifier $classifier")
        }
    }
}