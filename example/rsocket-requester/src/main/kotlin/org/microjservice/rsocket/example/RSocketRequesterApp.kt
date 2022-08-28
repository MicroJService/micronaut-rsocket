package org.microjservice.rsocket.example

import io.micronaut.runtime.Micronaut
/**
 * RSocket requester app
 *
 * @author leijuan
 */
fun main(args: Array<String>) {
    Micronaut.build()
        .args(*args)
        .packages("org.microjservice.user")
        .start()
}
