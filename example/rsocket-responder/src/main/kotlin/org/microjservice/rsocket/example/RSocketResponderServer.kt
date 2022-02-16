package org.microjservice.rsocket.example

import io.micronaut.context.annotation.Import
import io.micronaut.runtime.Micronaut.*

/**
 * RSocket Responder Server
 *
 * @author leijuan
 */


fun main(args: Array<String>) {
    build()
        .args(*args)
        .packages("org.microjservice.user")
        .start()
}
