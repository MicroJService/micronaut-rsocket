package org.microjservice.rsocket.alibaba

import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import org.microjservice.rsocket.core.common.unwrap
import java.util.stream.Collectors
import java.util.*

/**
 * environment with properties wrapper
 *
 * @author linux_china
 */
class EnvironmentProperties(private val env: Environment) : Properties() {
    override fun getProperty(key: String): String? {
        return env.getProperty(key, String::class.java).unwrap()
    }

    override fun getProperty(key: String, defaultValue: String): String {
        return env.getProperty(key, String::class.java).orElse(defaultValue)
    }

    override fun propertyNames(): Enumeration<*> {
        return Collections.enumeration(
            env.propertySources.stream()
                .map { obj: PropertySource -> obj.name }
                .collect(Collectors.toList()))
    }

    override fun stringPropertyNames(): Set<String> {
        val names: MutableSet<String> = HashSet()
        val e = propertyNames()
        while (e.hasMoreElements()) {
            val k = e.nextElement()
            if (k is String) {
                names.add(k)
            }
        }
        return names
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        if (!super.equals(other)) return false
        val that = other as EnvironmentProperties
        return env == that.env
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + env.hashCode()
        return result
    }
}