package org.microjservice.rsocket.core.annotation

import io.micronaut.context.annotation.Executable
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
//@Executable
annotation class RSocketService(
    val serviceInterface: KClass<*>,

    val name: String = "",

    val group: String = "",

    val version: String = "",

    val encoding: Array<String> = ["hessian", "json", "protobuf"],

    val tags: Array<String> = [],
)