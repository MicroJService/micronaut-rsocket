package org.microjservice.rsocket.alibaba.upstream

import com.alibaba.rsocket.RSocketRequesterSupport
import com.alibaba.rsocket.upstream.UpstreamManagerImpl
import org.microjservice.rsocket.core.common.micronaut.SmartLifecycle
import kotlin.Throws
import java.lang.RuntimeException
import reactor.core.publisher.Mono
import java.lang.Void
import com.alibaba.rsocket.events.AppStatusEvent
import com.alibaba.rsocket.cloudevents.RSocketCloudEventBuilder
import com.alibaba.rsocket.RSocketAppContext
import reactor.core.publisher.Flux
import com.alibaba.rsocket.upstream.UpstreamCluster
import java.lang.Exception

/**
 * SmartLifecycle UpstreamManager implementation with graceful shutdown support
 *
 * @author CoderYellow
 */
class SmartLifecycleUpstreamManagerImpl(rsocketRequesterSupport: RSocketRequesterSupport?) :
    UpstreamManagerImpl(rsocketRequesterSupport), SmartLifecycle {

    private var status = 0

    override fun isAutoStartUp() = true

    @Throws(Exception::class)
    override fun init() {
        if (this.status == 0) {
            super.init()
            this.status = 1
        }
    }

    override fun start() {
        try {
            init()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
        this.status = 1
    }

    override fun stop() {
        shutDownGracefully()
    }

    override fun isRunning(): Boolean {
        return status == 1
    }

    private fun shutDownGracefully() {
        try {
            close()
            if (requesterSupport().exposedServices().get().isNotEmpty()) {
                notifyShutdown().subscribe()
                // waiting for 15 seconds to broadcast shutdown message for service provider
                Thread.sleep(15000)
            }
        } catch (ignore: Exception) {

        }
    }

    private fun notifyShutdown(): Mono<Void> {
        val appStatusEventCloudEvent = RSocketCloudEventBuilder
            .builder(AppStatusEvent(RSocketAppContext.ID, AppStatusEvent.STATUS_STOPPED))
            .build()
        return Flux.fromIterable(findAllClusters()).flatMap { upstreamCluster: UpstreamCluster ->
            upstreamCluster.loadBalancedRSocket.fireCloudEventToUpstreamAll(appStatusEventCloudEvent)
        }
            .then()
    }
}