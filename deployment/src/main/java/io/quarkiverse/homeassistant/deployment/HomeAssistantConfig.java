package io.quarkiverse.homeassistant.deployment;

import java.util.Objects;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Allows configuring the HomeAssistant Integration.
 */
@ConfigMapping(prefix = "quarkus.homeassistant")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface HomeAssistantConfig {

    /**
     * Default docker image name.
     */
    String DEFAULT_IMAGE = "ghcr.io/maxandersen/private-demo:main";

    /**
     * If Dev Services for HomeAssistant has been explicitly enabled or disabled. Dev Services are generally enabled
     * by default, unless there is an existing configuration present.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The HomeAssistant container image to use.
     */
    @WithDefault(DEFAULT_IMAGE)
    String imageName();

    /**
     * Flag to control if verbose logging of HomeAssistant container is requested.
     */
    @WithDefault("true")
    boolean verbose();

    static boolean isEqual(HomeAssistantConfig d1, HomeAssistantConfig d2) {
        if (!Objects.equals(d1.enabled(), d2.enabled())) {
            return false;
        }
        if (!Objects.equals(d1.imageName(), d2.imageName())) {
            return false;
        }
        if (!Objects.equals(d1.verbose(), d2.verbose())) {
            return false;
        }
        return true;
    }


}