package org.microjservice.rsocket.alibaba.responder.invocation

import com.alibaba.rsocket.RSocketService
import com.alibaba.rsocket.rpc.LocalReactiveServiceCallerImpl
import io.micronaut.context.ApplicationContext
import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.processor.BeanDefinitionProcessor
import io.micronaut.context.processor.ExecutableMethodProcessor
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.inject.BeanDefinition
import io.micronaut.inject.ExecutableMethod
import jakarta.inject.Singleton
import org.microjservice.rsocket.alibaba.RSocketProperties
import org.microjservice.rsocket.core.common.unwrap

/**
 * RSocketService org.microjservice.rsocket.core.annotation processor
 *
 * @author CoderYellow
 */
class RSocketServiceAnnotationProcessor(
    private val rsocketProperties: RSocketProperties,
    private val applicationContext: ApplicationContext,
) :
    LocalReactiveServiceCallerImpl(), BeanDefinitionProcessor<RSocketService> {

    private fun registerRSocketService(rsocketServiceAnnotation: AnnotationValue<RSocketService>, bean: Any) {
        val serviceInterface = rsocketServiceAnnotation.classValue(RSocketService::serviceInterface.name).unwrap()!!

        val serviceName = rsocketServiceAnnotation.stringValue(RSocketService::name.name).orElseGet {
            serviceInterface.canonicalName
        }

        val group = rsocketServiceAnnotation.stringValue(RSocketService::group.name).orElseGet {
            rsocketProperties.group
        }

        val version = rsocketServiceAnnotation.stringValue(RSocketService::version.name).orElseGet {
            rsocketProperties.version
        }

        addProvider(group, serviceName, version, serviceInterface, bean)
    }

    override fun process(beanDefinition: BeanDefinition<*>, beanContext: BeanContext) {
        val reactiveService = beanDefinition.getAnnotation(
            RSocketService::class.java
        ) ?: throw RuntimeException("$beanDefinition must be annotated with ${RSocketService::class.java}")
        val bean = beanContext.getBean(beanDefinition)
        registerRSocketService(reactiveService, bean)
    }
}