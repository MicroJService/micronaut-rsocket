package org.microjservice.rsocket.alibaba.upstream

/**
 * rsocket requester support customizer
 *
 * @author CoderYellow
 */
fun interface RSocketRequesterSupportCustomizer {
    fun customize(builder: RSocketRequesterSupportBuilder?)
}