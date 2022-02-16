package org.microjservice.rsocket.alibaba

import com.alibaba.rsocket.upstream.UpstreamManager
import io.micronaut.management.health.indicator.HealthIndicator
import com.alibaba.rsocket.health.RSocketServiceHealth
import io.micronaut.management.health.indicator.HealthResult
import com.alibaba.rsocket.events.AppStatusEvent
import io.micronaut.health.HealthStatus
import com.alibaba.rsocket.invocation.RSocketRemoteServiceBuilder
import org.reactivestreams.Publisher

/**
 * RSocket Broker health indicator
 *
 * @author CoderYellow
 */
class RSocketBrokerHealthIndicator(
    private val rsocketEndpoint: RSocketEndpoint,
    upstreamManager: UpstreamManager?,
    brokers: String
) : HealthIndicator {

    private val rsocketServiceHealth: RSocketServiceHealth

    private val brokers: String

    init {
        rsocketServiceHealth = RSocketRemoteServiceBuilder
            .client(RSocketServiceHealth::class.java)
            .nativeImage()
            .upstreamManager(upstreamManager)
            .build()
        this.brokers = brokers
    }

    companion object {
        private const val RSOCKET_HEALTH_INDICATOR_NAME = "RSocket_Service_Health"
        private val OUT_OF_SERVICE = HealthStatus(
            "OUT_OF_SERVICE",
            "indicating that the component or subsystem has been taken out of service and should not be used.",
            false,
            999
        )
    }

    override fun getResult(): Publisher<HealthResult> {
        return rsocketServiceHealth.check(null)
            .map { result: Int? ->
                val brokerAlive = result != null && result == 1
                val localServicesAlive = rsocketEndpoint.rsocketServiceStatus != AppStatusEvent.STATUS_STOPPED
                val builder = if (brokerAlive && localServicesAlive) HealthResult.builder(
                    RSOCKET_HEALTH_INDICATOR_NAME,
                    HealthStatus.UP
                ) else HealthResult.builder(
                    RSOCKET_HEALTH_INDICATOR_NAME, OUT_OF_SERVICE
                )

                builder.details(
                    mapOf(
                        "brokers" to brokers,
                        "localServiceStatus" to AppStatusEvent.statusText(rsocketEndpoint.rsocketServiceStatus)
                    )
                )
                builder.build()
            }
            .onErrorReturn(
                HealthResult
                    .builder(RSOCKET_HEALTH_INDICATOR_NAME, HealthStatus.DOWN)
                    .details(mapOf("brokers" to brokers))
                    .build()
            )
    }

}