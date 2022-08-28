package org.microjservice.rsocket.example

import io.micronaut.runtime.Micronaut.build

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
