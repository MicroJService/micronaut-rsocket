package org.microjservice.rsocket.alibaba;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;


import java.util.*;

/**
 * environment with properties wrapper
 *
 * @author linux_china
 */
public class EnvironmentProperties extends Properties {
    private Environment env;

    public EnvironmentProperties(Environment env) {
        this.env = env;
    }

    @Override
    public String getProperty(String key) {
        return env.getProperty(key, String.class).get();
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return env.getProperty(key, String.class).orElse(defaultValue);
    }

    @Override
    public Enumeration<?> propertyNames() {
        Set<String> names = new HashSet<>();
        for (PropertySource propertySource : env.getPropertySources()) {
            if (propertySource instanceof EnumerablePropertySource) {
                Collections.addAll(names, propertySource.getPropertyNames());
            }
        }
        env.getPropertySources().stream().flatMap()
        return Collections.enumeration(names);
    }

    @Override
    public Set<String> stringPropertyNames() {
        Set<String> names = new HashSet<>();
        for (Enumeration<?> e = propertyNames(); e.hasMoreElements(); ) {
            Object k = e.nextElement();
            if (k instanceof String) {
                names.add((String) k);
            }
        }
        return names;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        EnvironmentProperties that = (EnvironmentProperties) o;

        return Objects.equals(env, that.env);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (env != null ? env.hashCode() : 0);
        return result;
    }
}
