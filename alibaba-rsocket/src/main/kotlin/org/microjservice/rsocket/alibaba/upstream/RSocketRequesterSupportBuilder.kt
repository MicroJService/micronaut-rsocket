package org.microjservice.rsocket.alibaba.upstream

import io.rsocket.plugins.RSocketInterceptor
import org.microjservice.rsocket.alibaba.upstream.RSocketRequesterSupportBuilder
import com.alibaba.rsocket.RSocketRequesterSupport

/**
 * RSocket Requester support builder
 *
 * @author CoderYellow
 */
interface RSocketRequesterSupportBuilder {
    fun addResponderInterceptor(interceptor: RSocketInterceptor): RSocketRequesterSupportBuilder

    fun addRequesterInterceptor(interceptor: RSocketInterceptor): RSocketRequesterSupportBuilder

    fun build(): RSocketRequesterSupport
}