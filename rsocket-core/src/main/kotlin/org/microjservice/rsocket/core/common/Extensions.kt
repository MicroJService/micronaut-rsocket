package org.microjservice.rsocket.core.common

import java.util.*
import kotlin.contracts.contract

fun <T> Optional<T>.unwrap(): T? = orElse(null)

inline fun <T> Collection<T>?.ifNotNullAndEmpty(block: Collection<T>.() -> Unit) {
    this
        ?.takeIf { isNotEmpty() }
        ?.apply { block(this) }
}
