package org.microjservice.rsocket.core.annotation

import com.alibaba.rsocket.invocation.RSocketRemoteServiceBuilder
import com.alibaba.rsocket.upstream.UpstreamManager
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class RSocketClientIntroductionAdvice(
    private val upstreamManager: UpstreamManager
) :
    MethodInterceptor<Any, Any> {

    private val rsocketClients = ConcurrentHashMap<Class<Any>, Any>()

    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        val client = rsocketClients.computeIfAbsent(context.declaringType) { getRsocketClient(context) }
        return context.executableMethod.invoke(client, *context.parameterValues)
    }

    private fun getRsocketClient(context: MethodInvocationContext<Any, Any>): Any {
        val annotationMetadata = context.annotationMetadata
        return RSocketRemoteServiceBuilder
            .client(context.declaringType)
            .group(annotationMetadata.stringValue(RSocketClient::class.java, RSocketClient::group.name).orElse(""))
            .service(annotationMetadata.stringValue(RSocketClient::class.java, RSocketClient::service.name).orElse(""))
            .version(annotationMetadata.stringValue(RSocketClient::class.java, RSocketClient::version.name).orElse(""))
            .upstreamManager(upstreamManager)
            .build()
    }

}
