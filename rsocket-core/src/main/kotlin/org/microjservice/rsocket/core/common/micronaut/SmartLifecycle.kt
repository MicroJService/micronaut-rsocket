package org.microjservice.rsocket.core.common.micronaut

/**
 * similar to org.springframework.context.SmartLifecycle
 *
 * @author CoderYellow
 */
interface SmartLifecycle {

    fun isAutoStartUp(): Boolean

    fun start()

    fun stop()

    fun isRunning(): Boolean
}
