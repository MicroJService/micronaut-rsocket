package org.microjservice.rsocket.alibaba

import com.alibaba.rsocket.RSocketRequesterSupport
import io.micronaut.context.ApplicationContext
import io.rsocket.plugins.RSocketInterceptor
import com.alibaba.rsocket.transport.NetworkUtil
import com.alibaba.rsocket.RSocketAppContext
import com.alibaba.rsocket.RSocketService
import com.alibaba.rsocket.metadata.RSocketCompositeMetadata
import com.alibaba.rsocket.metadata.RSocketMimeType
import com.alibaba.rsocket.metadata.ServiceRegistryMetadata
import com.alibaba.rsocket.metadata.BearerTokenMetadata
import io.rsocket.util.ByteBufPayload
import io.netty.buffer.Unpooled
import io.micronaut.inject.qualifiers.Qualifiers
import com.alibaba.rsocket.ServiceLocator
import io.micronaut.inject.BeanDefinition
import com.alibaba.rsocket.health.RSocketServiceHealth
import com.alibaba.rsocket.observability.MetricsService
import java.util.stream.Collectors
import com.alibaba.rsocket.invocation.RSocketRemoteServiceBuilder
import com.alibaba.rsocket.cloudevents.CloudEventImpl
import com.alibaba.rsocket.events.ServicesExposedEvent
import com.alibaba.rsocket.metadata.AppMetadata
import io.micronaut.context.BeanContext
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.runtime.event.annotation.EventListener
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.rsocket.Payload
import io.rsocket.SocketAcceptor
import org.microjservice.rsocket.core.common.micronaut.ApplicationContextAware
import java.lang.Exception
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.regex.Pattern

/**
 * RSocket Requester Support implementation: setup payload, published service, token and app info
 *
 * @author leijuan
 */
open class RSocketRequesterSupportImpl(
    protected val properties: RSocketProperties,
    protected val env: Properties,
    socketAcceptor: SocketAcceptor,
) : RSocketRequesterSupport
    , ApplicationContextAware
{
    private lateinit var applicationContext: ApplicationContext
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    protected var appName: String? =
        env.getProperty("micronaut.application.name") ?: env.getProperty("application.name")
    protected var jwtToken: CharArray? = env.getProperty("rsocket.jwt-token", "").toCharArray()

    protected var socketAcceptor: SocketAcceptor
    protected var responderInterceptors: MutableList<RSocketInterceptor> = ArrayList()
    protected var requestInterceptors: MutableList<RSocketInterceptor> = ArrayList()

    override fun originUri(): URI {
        return URI.create(
            properties.schema + "://" + NetworkUtil.LOCAL_IP + ":" + properties.port
                    + "?appName=" + appName
                    + "&uuid=" + RSocketAppContext.ID
        )
    }

    override fun setupPayload(serviceId: String): Supplier<Payload> {
        return Supplier {

            //composite metadata with app metadata
            val compositeMetadata = RSocketCompositeMetadata.from(appMetadata)
            //add published in setup payload
            val serviceLocators = exposedServices().get()
            if (!compositeMetadata.contains(RSocketMimeType.ServiceRegistry) && serviceLocators.isNotEmpty()) {
                val serviceRegistryMetadata = ServiceRegistryMetadata()
                serviceRegistryMetadata.published = serviceLocators
                compositeMetadata.addMetadata(serviceRegistryMetadata)
            }
            // authentication for broker
            if (serviceId == "*") {
                if (jwtToken != null && jwtToken!!.size > 0) {
                    compositeMetadata.addMetadata(BearerTokenMetadata(jwtToken))
                }
            }
            ByteBufPayload.create(Unpooled.EMPTY_BUFFER, compositeMetadata.content)
        }
    }

    override fun exposedServices(): Supplier<Set<ServiceLocator>> {
        return Supplier {
            val beanDefinitions =
                applicationContext.getBeanDefinitions(Qualifiers.byStereotype(RSocketService::class.java))
            val services = beanDefinitions
                .stream()
                .filter { beanDefinition: BeanDefinition<*> -> !(beanDefinition.beanType == RSocketServiceHealth::class.java || beanDefinition.beanType == MetricsService::class.java) }
                .map { definition: BeanDefinition<*> ->
                    definition.getAnnotation(
                        RSocketService::class.java
                    )
                }
                .filter { obj: AnnotationValue<RSocketService?>? -> Objects.nonNull(obj) }
                .map { annotationValue: AnnotationValue<RSocketService?> ->
                    val serviceName = annotationValue.classValue(RSocketService::serviceInterface.name)
                        .map { obj: Class<*> -> obj.canonicalName }
                        .orElseGet { annotationValue.stringValue("name").orElse("") }
                    ServiceLocator(
                        properties.group,
                        serviceName,
                        properties.version,
                        annotationValue.stringValues("tags")
                    )
                }
                .collect(Collectors.toSet())

             services

        }
    }

    override fun subscribedServices(): Supplier<Set<ServiceLocator>> {
        return Supplier { RSocketRemoteServiceBuilder.CONSUMED_SERVICES }
    }

    override fun servicesExposedEvent(): Supplier<CloudEventImpl<ServicesExposedEvent>?> {
        return Supplier {
            val serviceLocators: Collection<ServiceLocator> = exposedServices().get()
            if (serviceLocators.isEmpty()) {
                null
            } else {
                ServicesExposedEvent.convertServicesToCloudEvent(serviceLocators)
            }
        }
    }

    //app metadata
    private val appMetadata: AppMetadata
        get() {
            //app metadata
            val appMetadata = AppMetadata()
            appMetadata.uuid = RSocketAppContext.ID
            appMetadata.name = appName
            appMetadata.ip = NetworkUtil.LOCAL_IP
            appMetadata.device = "MicronautApp"
            //brokers
            appMetadata.brokers = properties.brokers
            appMetadata.topology = properties.topology
            appMetadata.p2pServices = properties.p2pServices
            //web port
            appMetadata.webPort = env.getProperty("server.port", "0").toInt()
            appMetadata.managementPort = appMetadata.webPort
            //management port
            if (env.getProperty("management.server.port") != null) {
                appMetadata.managementPort = env.getProperty("management.server.port").toInt()
            }
            if (appMetadata.webPort <= 0) {
                appMetadata.webPort = RSocketAppContext.webPort
            }
            if (appMetadata.managementPort <= 0) {
                appMetadata.managementPort = RSocketAppContext.managementPort
            }
            if (RSocketAppContext.rsocketPorts != null) {
                appMetadata.rsocketPorts = RSocketAppContext.rsocketPorts
            } else {
                if (properties.port != null) appMetadata.rsocketPorts = Collections.singletonMap(
                    properties.port, properties.schema
                )
            }
            //labels
            appMetadata.metadata = HashMap()
            env.stringPropertyNames().forEach(Consumer { key: String ->
                if (key.startsWith("rsocket.metadata.")) {
                    val parts = key.split(Pattern.compile("[=:]"), 2).toTypedArray()
                    appMetadata.metadata[parts[0].trim { it <= ' ' }.replace("rsocket.metadata.", "")] =
                        env.getProperty(key)
                }
            })
            //power unit
            if (appMetadata.getMetadata("power-rating") != null) {
                appMetadata.powerRating = appMetadata.getMetadata("power-rating").toInt()
            }
            //humans.md
            val humansMd = this.javaClass.getResource("/humans.md")
            if (humansMd != null) {
                try {
                    humansMd.openStream().use { inputStream ->
                        val bytes = ByteArray(inputStream.available())
                        inputStream.read(bytes)
                        inputStream.close()
                        appMetadata.humansMd = String(bytes, StandardCharsets.UTF_8)
                    }
                } catch (ignore: Exception) {
                }
            }
            return appMetadata
        }

    override fun socketAcceptor(): SocketAcceptor {
        return socketAcceptor
    }

    override fun responderInterceptors(): List<RSocketInterceptor> {
        return responderInterceptors
    }

    fun addResponderInterceptor(interceptor: RSocketInterceptor) {
        responderInterceptors.add(interceptor)
    }

    override fun requestInterceptors(): List<RSocketInterceptor> {
        return requestInterceptors
    }

    fun addRequesterInterceptor(interceptor: RSocketInterceptor) {
        requestInterceptors.add(interceptor)
    }

    init {
        this.socketAcceptor = socketAcceptor
    }

}