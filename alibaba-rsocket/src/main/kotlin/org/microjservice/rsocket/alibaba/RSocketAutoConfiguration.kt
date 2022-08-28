package org.microjservice.rsocket.alibaba

import brave.Tracing
import com.alibaba.rsocket.RSocketAppContext
import com.alibaba.rsocket.RSocketRequesterSupport
import com.alibaba.rsocket.cloudevents.CloudEventImpl
import com.alibaba.rsocket.events.CloudEventsConsumer
import com.alibaba.rsocket.events.CloudEventsProcessor
import com.alibaba.rsocket.health.RSocketServiceHealth
import com.alibaba.rsocket.listen.RSocketResponderHandlerFactory
import com.alibaba.rsocket.observability.MetricsService
import com.alibaba.rsocket.rpc.LocalReactiveServiceCaller
import com.alibaba.rsocket.rpc.RSocketResponderHandler
import com.alibaba.rsocket.upstream.ServiceInstancesChangedEventConsumer
import com.alibaba.rsocket.upstream.UpstreamCluster
import com.alibaba.rsocket.upstream.UpstreamClusterChangedEventConsumer
import com.alibaba.rsocket.upstream.UpstreamManager
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanProvider
import io.micronaut.context.annotation.*
import io.micronaut.context.env.Environment
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.management.health.indicator.HealthIndicator
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.rsocket.ConnectionSetupPayload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import jakarta.inject.Named
import org.microjservice.rsocket.alibaba.health.RSocketServiceHealthImpl
import org.microjservice.rsocket.alibaba.observability.MetricsServicePrometheusImpl
import org.microjservice.rsocket.alibaba.responder.RSocketServicesPublishHook
import org.microjservice.rsocket.alibaba.responder.invocation.RSocketServiceAnnotationProcessor
import org.microjservice.rsocket.alibaba.upstream.JwtTokenNotFoundException
import org.microjservice.rsocket.alibaba.upstream.RSocketRequesterSupportBuilderImpl
import org.microjservice.rsocket.alibaba.upstream.RSocketRequesterSupportCustomizer
import org.microjservice.rsocket.alibaba.upstream.SmartLifecycleUpstreamManagerImpl
import org.microjservice.rsocket.core.common.ifNotNullAndEmpty
import org.microjservice.rsocket.core.common.micronaut.ApplicationContextAware
import org.microjservice.rsocket.core.common.unwrap
import reactor.core.publisher.Mono
import reactor.extra.processor.TopicProcessor
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * RSocket Auto configuration: listen, upstream manager, handler etc
 *
 * @author leijuan
 */
@Factory
class RSocketAutoConfiguration(
    private val properties: RSocketProperties,
    private val applicationContext: ApplicationContext
) {
    private var serverPort = 0
    private var managementServerPort = 0

    // section cloudevents processor
    @Bean
    @Named("reactiveCloudEventProcessor")
    fun reactiveCloudEventProcessor(): TopicProcessor<CloudEventImpl<*>> {
        return TopicProcessor.builder<CloudEventImpl<*>>().name("cloud-events-processor").build()
    }

    @Bean
    fun cloudEventsProcessor(
        @Named("reactiveCloudEventProcessor") eventProcessor: TopicProcessor<CloudEventImpl<*>?>,
        consumers: BeanProvider<List<CloudEventsConsumer?>>
    ): CloudEventsProcessor {
        return CloudEventsProcessor(eventProcessor, consumers.get().stream().collect(Collectors.toList()))
    }

    @Bean
    fun upstreamClusterChangedEventConsumer(upstreamManager: UpstreamManager): UpstreamClusterChangedEventConsumer {
        return UpstreamClusterChangedEventConsumer(upstreamManager)
    }

    @Bean
    fun serviceInstancesChangedEventConsumer(upstreamManager: UpstreamManager): ServiceInstancesChangedEventConsumer {
        return ServiceInstancesChangedEventConsumer(upstreamManager)
    }

    @Bean
    fun cloudEventToListenerConsumer(applicationEventPublisher: ApplicationEventPublisher<CloudEventImpl<*>>): CloudEventToListenerConsumer {
        return CloudEventToListenerConsumer(applicationEventPublisher)
    }

    /**
     * socket responder handler as SocketAcceptor bean.
     * To validate connection, please use RSocketListenerCustomizer and add AcceptorInterceptor by addSocketAcceptorInterceptor api
     *
     * @param serviceCaller  service caller
     * @param eventProcessor event processor
     * @return handler factor
     */
    @Bean
    @Requires(missingBeans = [RSocketResponderHandlerFactory::class, Tracing::class])
    fun rsocketResponderHandlerFactory(
        serviceCaller: LocalReactiveServiceCaller,
        @Named("reactiveCloudEventProcessor") eventProcessor: TopicProcessor<CloudEventImpl<*>?>
    ): RSocketResponderHandlerFactory {
        return RSocketResponderHandlerFactory { setupPayload: ConnectionSetupPayload, requester: RSocket ->
            Mono.fromCallable {
                RSocketResponderHandler(
                    serviceCaller,
                    eventProcessor,
                    requester,
                    setupPayload
                )
            }
        }
    }

    @Bean
    @Requires(missingBeans = [RSocketResponderHandlerFactory::class], beans = [Tracing::class])
    fun rsocketResponderHandlerFactoryWithZipkin(
        serviceCaller: LocalReactiveServiceCaller,
        @Named("reactiveCloudEventProcessor") eventProcessor: TopicProcessor<CloudEventImpl<*>?>
    ): RSocketResponderHandlerFactory {
        return RSocketResponderHandlerFactory { setupPayload: ConnectionSetupPayload, requester: RSocket ->
            Mono.fromCallable {
                val responderHandler = RSocketResponderHandler(serviceCaller, eventProcessor, requester, setupPayload)
                val tracing = applicationContext.getBean(Tracing::class.java)
                responderHandler.setTracer(tracing.tracer())
                responderHandler
            }
        }
    }

    @Bean
    @Requires(missingBeans = [RSocketRequesterSupport::class])
    fun rsocketRequesterSupport(
        properties: RSocketProperties,
        environment: Environment,
        socketAcceptor: SocketAcceptor,
        customizers: BeanProvider<RSocketRequesterSupportCustomizer>,
        applicationContext: ApplicationContext,
    ): RSocketRequesterSupportImpl {
        val builder = RSocketRequesterSupportBuilderImpl(
            properties, EnvironmentProperties(
                environment
            ), socketAcceptor
        )
        customizers.forEach(Consumer { customizer: RSocketRequesterSupportCustomizer -> customizer.customize(builder) })
        return builder.build() as RSocketRequesterSupportImpl
    }

    @Bean
    @Requires(missingBeans = [LocalReactiveServiceCaller::class])
    @Context
    fun rSocketServiceAnnotationProcessor(
        rsocketProperties: RSocketProperties,
        applicationContext: ApplicationContext
    ): RSocketServiceAnnotationProcessor {
        return RSocketServiceAnnotationProcessor(rsocketProperties, applicationContext)
    }

    //   todo  @Bean(initMethod = "init")
    @Bean
    @Throws(Exception::class)
    fun rsocketUpstreamManager(rsocketRequesterSupport: RSocketRequesterSupport): UpstreamManager {
        val upstreamManager = SmartLifecycleUpstreamManagerImpl(rsocketRequesterSupport)
        properties.brokers.ifNotNullAndEmpty {
            if (properties.jwtToken.isNullOrEmpty()) {
                throw JwtTokenNotFoundException()
            }
            val cluster = UpstreamCluster(null, "*", null)
            cluster.uris = properties.brokers
            upstreamManager.add(cluster)
        }

        upstreamManager.p2pServices = properties.p2pServices

        properties.routes.ifNotNullAndEmpty {
            forEach { upstreamManager.add(UpstreamCluster(it.group, it.service, it.version)) }
        }

        if (properties.routes != null && properties.routes!!.isNotEmpty()) {
            for (route in properties.routes!!) {
                val cluster = UpstreamCluster(route.group, route.service, route.version)
                cluster.uris = route.uris
                upstreamManager.add(cluster)
            }
        }
        upstreamManager.init();
        return upstreamManager
    }

    @Bean
    @Requires(property = "rsocket.brokers")
    fun rsocketBrokerHealth(
        rsocketEndpoint: RSocketEndpoint,
        upstreamManager: UpstreamManager,
        @Value("\${rsocket.brokers}") brokers: String
    ): RSocketBrokerHealthIndicator {
        return RSocketBrokerHealthIndicator(rsocketEndpoint, upstreamManager, brokers)
    }

    @Bean
    fun rsocketEndpoint(
        upstreamManager: UpstreamManager,
        rsocketRequesterSupport: RSocketRequesterSupport
    ): RSocketEndpoint {
        return RSocketEndpoint(properties, upstreamManager, rsocketRequesterSupport)
    }

    @Bean
    @Requires(classes = [PrometheusMeterRegistry::class])
    fun metricsService(meterRegistry: PrometheusMeterRegistry): MetricsService {
        return MetricsServicePrometheusImpl(meterRegistry)
    }

    @Bean
    fun rsocketServicesPublishHook(
        upstreamManager: UpstreamManager,
        rsocketRequesterSupport: RSocketRequesterSupport
    ): RSocketServicesPublishHook {
        return RSocketServicesPublishHook(upstreamManager, rsocketRequesterSupport)
    }

    @Bean
    @Requires(missingBeans = [RSocketServiceHealth::class])
    fun rsocketServiceHealth(healthIndicators: List<HealthIndicator>): RSocketServiceHealth {
        return RSocketServiceHealthImpl(healthIndicators)
    }

    @Bean
    fun serverStartupEventApplicationListener(): ApplicationEventListener<ServerStartupEvent> {
        return ApplicationEventListener { serverStartupEvent: ServerStartupEvent ->
            val listenPort = serverStartupEvent.source.port
            val managementPort =
                serverStartupEvent.source.applicationContext.environment
                    .get("endpoints.all.port", Int::class.java).unwrap() ?: listenPort

            serverPort = listenPort
            RSocketAppContext.webPort = listenPort
            managementServerPort = managementPort
            RSocketAppContext.managementPort = managementPort
        }
    }
}