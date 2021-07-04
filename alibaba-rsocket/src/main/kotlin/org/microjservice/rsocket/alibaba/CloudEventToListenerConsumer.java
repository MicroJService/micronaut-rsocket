package org.microjservice.rsocket.alibaba;

import com.alibaba.rsocket.cloudevents.CloudEventImpl;
import com.alibaba.rsocket.events.CloudEventsConsumer;
import io.micronaut.context.event.ApplicationEventPublisher;

import reactor.core.publisher.Mono;

/**
 * CloudEvent to @EventListener handler
 *
 * @author leijuan
 */
public class CloudEventToListenerConsumer implements CloudEventsConsumer {
    private ApplicationEventPublisher eventPublisher;

    @Override
    public boolean shouldAccept(CloudEventImpl<?> cloudEvent) {
        return true;
    }

    @Override
    public Mono<Void> accept(CloudEventImpl<?> cloudEvent) {
        return Mono.fromRunnable(() -> {
            eventPublisher.publishEvent(cloudEvent);
        });
    }
}
