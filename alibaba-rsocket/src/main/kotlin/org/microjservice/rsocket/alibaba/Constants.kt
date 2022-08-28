package org.microjservice.rsocket.alibaba

import io.micronaut.health.HealthStatus

/**
 * Contants
 *
 * @author Coder Yellow
 * @since 0.1.0
 */
object Constants {
    const val RSOCKET = "rsocket"

    object ENVIRONMENT {
        const val PREFIX = RSOCKET
    }

    object LETTER {
        const val COMMA = ","
    }

    object HEALTH_STATUS {
        public val OUT_OF_SERVICE = HealthStatus(
            "OUT_OF_SERVICE",
            "indicating that the component or subsystem has been taken out of service and should not be used.",
            false,
            999
        )
    }
}