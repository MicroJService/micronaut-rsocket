package org.microjservice.rsocket.core.common.micronaut

import io.micronaut.context.event.ShutdownEvent
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton

/**
 *
 *
 * @author CoderYellow
 */
@Singleton
class SmartLifecycleProcessor(private val smartLifecycleList: List<SmartLifecycle>) {

    @EventListener
    fun onStartUp(event: StartupEvent) {
        smartLifecycleList.parallelStream()
            .filter { it.isAutoStartUp() }
            .forEach { it.start() }
    }

    @EventListener
    fun onShutDown(event: ShutdownEvent) {
        smartLifecycleList.parallelStream()
            .filter { it.isRunning() }
            .forEach { it.stop() }
    }
}