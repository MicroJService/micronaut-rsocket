package org.microjservice.rsocket.alibaba.upstream

import com.alibaba.rsocket.upstream.UpstreamCluster
import com.alibaba.rsocket.upstream.UpstreamManager
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.microjservice.rsocket.core.common.micronaut.SmartLifecycleProcessor
import spock.lang.Specification

/**
 *
 *
 * @author CoderYellow
 */
@MicronautTest(packages = ["org.microjservice.rsocket.core.common.micronaut"])
//@Property(name = "micronaut.application.name", value = "RSocketServiceAnnotationProcessorSpec")
//@Property(name = "rsocket.brokers", value = "tcp://127.0.0.1:9999")
class SmartLifecycleUpstreamManagerImplSpec extends Specification {

    @Inject
    UpstreamManager upstreamManager

    @Inject
    SmartLifecycleProcessor smartLifecycleProcessor

    void "should register services to broker"() {
        def broker = upstreamManager.findBroker()
        upstreamManager.getRSocket()
        expect:
        broker != null

    }

}
