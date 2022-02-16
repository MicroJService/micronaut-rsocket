package org.microjservice.rsocket.alibaba.responder.invocation

import com.alibaba.rsocket.rpc.ReactiveMethodHandler
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

/**
 *
 *
 * @author CoderYellow
 */
@MicronautTest
class RSocketServiceAnnotationProcessorSpec extends Specification {
    @Inject
    RSocketServiceAnnotationProcessor rSocketServiceAnnotationProcessor


    void "test register RSocket services"() {
        given:
        def method = rSocketServiceAnnotationProcessor.getInvokeMethod(RSocketServiceFactory.IRSocketService.canonicalName, "greeting")
        expect:
        method.serviceName == RSocketServiceFactory.IRSocketService.canonicalName

    }


}
