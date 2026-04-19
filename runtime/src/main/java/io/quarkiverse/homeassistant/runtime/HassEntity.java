package io.quarkiverse.homeassistant.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

/**
 * CDI qualifier for injecting Home Assistant entities.
 *
 * <pre>
 * &#64;Inject
 * &#64;HassEntity("light.kitchen")
 * HAEntity kitchenLight;
 * </pre>
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
public @interface HassEntity {

    /**
     * The Home Assistant entity ID, e.g. {@code "light.kitchen"} or {@code "sensor.temperature"}.
     */
    String value();
}
