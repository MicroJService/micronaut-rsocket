package org.microjservice.rsocket.alibaba.responder

import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import com.alibaba.rsocket.upstream.UpstreamManager
import com.alibaba.rsocket.RSocketRequesterSupport
import com.alibaba.rsocket.events.AppStatusEvent
import com.alibaba.rsocket.cloudevents.RSocketCloudEventBuilder
import com.alibaba.rsocket.RSocketAppContext
import com.alibaba.rsocket.ServiceLocator
import com.alibaba.rsocket.events.PortsUpdateEvent
import java.lang.Void
import com.alibaba.rsocket.observability.RsocketErrorCode
import org.microjservice.rsocket.alibaba.Constants.LETTER.COMMA
import org.slf4j.LoggerFactory
import java.util.stream.Collectors

/**
 * RSocket services publish hook
 *
 * @author CoderYellow
 */
class RSocketServicesPublishHook(
    private val upstreamManager: UpstreamManager,
    private val rsocketRequesterSupport: RSocketRequesterSupport
) : ApplicationEventListener<ServerStartupEvent> {

    companion object {
        private val log = LoggerFactory.getLogger(RSocketServicesPublishHook::class.java)
    }

    override fun onApplicationEvent(event: ServerStartupEvent) {
        val brokerCluster = upstreamManager.findBroker() ?: return
        //rsocket broker cluster logic
        val appStatusEventCloudEvent = RSocketCloudEventBuilder
            .builder(AppStatusEvent(RSocketAppContext.ID, AppStatusEvent.STATUS_SERVING))
            .build()
        val loadBalancedRSocket = brokerCluster.loadBalancedRSocket
        //ports update
        val serverPort = event.source.port
        if (serverPort == 0) {
            if (RSocketAppContext.webPort > 0 || RSocketAppContext.managementPort > 0 || RSocketAppContext.rsocketPorts != null) {
                val portsUpdateEvent = PortsUpdateEvent()
                portsUpdateEvent.appId = RSocketAppContext.ID
                portsUpdateEvent.webPort = RSocketAppContext.webPort
                portsUpdateEvent.managementPort = RSocketAppContext.managementPort
                portsUpdateEvent.rsocketPorts = RSocketAppContext.rsocketPorts
                val portsUpdateCloudEvent = RSocketCloudEventBuilder
                    .builder(portsUpdateEvent)
                    .build()
                loadBalancedRSocket.fireCloudEventToUpstreamAll(portsUpdateCloudEvent)
                    .doOnSuccess {
                        log.info(
                            RsocketErrorCode.message(
                                "RST-301200",
                                loadBalancedRSocket.activeUris
                            )
                        )
                    }
                    .subscribe()
            }
        }
        // app status
        loadBalancedRSocket.fireCloudEventToUpstreamAll(appStatusEventCloudEvent)
            .doOnSuccess {
                log.info(
                    RsocketErrorCode.message(
                        "RST-301200",
                        loadBalancedRSocket.activeUris
                    )
                )
            }
            .subscribe()
        // service exposed
        val servicesExposedEventCloudEvent = rsocketRequesterSupport.servicesExposedEvent().get()
        if (servicesExposedEventCloudEvent != null) {
            loadBalancedRSocket.fireCloudEventToUpstreamAll(servicesExposedEventCloudEvent)
                .doOnSuccess {
                    val exposedServices = rsocketRequesterSupport.exposedServices().get()
                        .stream()
                        .map { serviceLocator: ServiceLocator -> serviceLocator.gsv }
                        .collect(Collectors.joining(COMMA))
                    log.info(RsocketErrorCode.message("RST-301201", exposedServices, loadBalancedRSocket.activeUris))
                }.subscribe()
        }
    }

}