package org.microjservice.rsocket.alibaba.health

import com.alibaba.rsocket.RSocketService
import com.alibaba.rsocket.health.RSocketServiceHealth
import io.micronaut.management.health.indicator.HealthIndicator
import reactor.core.publisher.Mono
import reactor.core.publisher.Flux
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.health.HealthStatus

/**
 * RSocket service health default implementation
 *
 * @author leijuan
 */
@RSocketService(serviceInterface = RSocketServiceHealth::class)
class RSocketServiceHealthImpl(private val healthIndicators: List<HealthIndicator>) : RSocketServiceHealth {

    override fun check(serviceName: String?): Mono<Int> {
        return Flux.fromIterable(healthIndicators)
            .flatMap { obj: HealthIndicator -> obj.result }
            .map { obj: HealthResult -> obj.status }
            .all { status: HealthStatus -> status === HealthStatus.UP }
            .map { result: Boolean -> if (result) RSocketServiceHealth.SERVING_STATUS else RSocketServiceHealth.DOWN_STATUS }
    }

}