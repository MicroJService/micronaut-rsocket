package org.microjservice.rsocket.alibaba.observability

import com.alibaba.rsocket.RSocketService
import com.alibaba.rsocket.observability.MetricsService
import io.micrometer.prometheus.PrometheusMeterRegistry
import reactor.core.publisher.Mono

/**
 * metrics service Prometheus implementation
 *
 * @author leijuan
 */
@RSocketService(serviceInterface = MetricsService::class)
class MetricsServicePrometheusImpl(private val meterRegistry: PrometheusMeterRegistry) : MetricsService {

    override fun scrape(): Mono<String> {
        return Mono.fromCallable { meterRegistry.scrape() }
    }
}