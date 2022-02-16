package org.microjservice.rsocket.alibaba.upstream

import org.microjservice.rsocket.alibaba.RSocketProperties
import com.alibaba.rsocket.RSocketRequesterSupport
import io.rsocket.SocketAcceptor
import io.rsocket.plugins.RSocketInterceptor
import org.microjservice.rsocket.alibaba.RSocketRequesterSupportImpl
import java.util.*

/**
 * RSocket Requester support  builder implementation
 *
 * @author CoderYellow
 */
class RSocketRequesterSupportBuilderImpl(
    properties: RSocketProperties,
    env: Properties,
    socketAcceptor: SocketAcceptor
) : RSocketRequesterSupportBuilder {

    private var requesterSupport: RSocketRequesterSupport
    private val requesterInterceptors: MutableList<RSocketInterceptor> = ArrayList()
    private val responderInterceptors: MutableList<RSocketInterceptor> = ArrayList()

    init {
        requesterSupport = RSocketRequesterSupportImpl(properties, env, socketAcceptor)
    }

    fun requesterSupport(requesterSupport: RSocketRequesterSupport): RSocketRequesterSupportBuilder {
        this.requesterSupport = requesterSupport
        return this
    }

    override fun addResponderInterceptor(interceptor: RSocketInterceptor): RSocketRequesterSupportBuilder {
        responderInterceptors.add(interceptor)
        return this
    }

    override fun addRequesterInterceptor(interceptor: RSocketInterceptor): RSocketRequesterSupportBuilder {
        requesterInterceptors.add(interceptor)
        return this
    }

    override fun build(): RSocketRequesterSupport {

        requesterSupport.responderInterceptors().addAll(responderInterceptors)

        requesterSupport.requestInterceptors().addAll(requesterInterceptors)

        return requesterSupport
    }


}