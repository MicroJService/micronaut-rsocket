package org.microjservice.rsocket.core.annotation

import io.micronaut.aop.Introduction
import io.micronaut.context.annotation.Type
import io.micronaut.retry.annotation.Recoverable
import jakarta.inject.Qualifier
import jakarta.inject.Singleton


@MustBeDocumented
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@Introduction
@Type(RSocketClientIntroductionAdvice::class)
@Recoverable
@Qualifier
@Singleton
annotation class RSocketClient(
    val service: String = "",

    val group: String = "",

    val version: String = "",
)
