package org.microjservice.rsocket.core.common.micronaut

import io.micronaut.context.ApplicationContext

/**
 * similar to org.springframework.context.ApplicationContextAware
 *
 * @author CoderYellow
 */
@FunctionalInterface
fun interface ApplicationContextAware {

    fun setApplicationContext(applicationContext: ApplicationContext)

}