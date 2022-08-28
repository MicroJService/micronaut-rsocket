package org.microjservice.rsocket.core.common.micronaut

import io.micronaut.context.ApplicationContext
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.context.event.BeanInitializedEventListener
import io.micronaut.context.event.BeanInitializingEvent
import jakarta.inject.Singleton

/**
 *
 *
 * @author CoderYellow
 */
@Singleton
class ApplicationContextAwareInitializer : BeanCreatedEventListener<ApplicationContextAware> {

    override fun onCreated(event: BeanCreatedEvent<ApplicationContextAware>): ApplicationContextAware {
        event.bean.setApplicationContext(event.source.getBean(ApplicationContext::class.java))
        return event.bean
    }
}