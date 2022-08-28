package org.microjservice.rsocket.alibaba

import com.alibaba.rsocket.events.CloudEventsConsumer
import io.micronaut.context.event.ApplicationEventPublisher
import com.alibaba.rsocket.cloudevents.CloudEventImpl
import reactor.core.publisher.Mono
import java.lang.Void

/**
 * CloudEvent to @EventListener handler
 *
 * @author leijuan
 */
class CloudEventToListenerConsumer(private val eventPublisher: ApplicationEventPublisher<CloudEventImpl<*>>) :
    CloudEventsConsumer {

    override fun shouldAccept(cloudEvent: CloudEventImpl<*>) = true

    override fun accept(cloudEvent: CloudEventImpl<*>): Mono<Void> {
        return Mono.fromRunnable { eventPublisher.publishEvent(cloudEvent) }
    }

}