package org.microjservice.rsocket.alibaba.responder.invocation;

import io.micronaut.context.annotation.Factory;
import org.microjservice.rsocket.core.annotation.RSocketService;

/**
 * @author CoderYellow
 */
@Factory
public class RSocketServiceFactory {

    public static interface IRSocketService {

        String greeting(String name);
    }

    @RSocketService(serviceInterface = IRSocketService.class)
    public static class RSocketServiceImpl implements IRSocketService {

        @Override
        public String greeting(String name) {
            return "Hello " + name;
        }
    }
}
