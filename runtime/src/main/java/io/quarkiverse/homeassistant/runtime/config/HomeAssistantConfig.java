package io.quarkiverse.homeassistant.runtime.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithParentName;

@ConfigRoot(phase= ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.homeassistant")
public interface HomeAssistantConfig {

    /**
     * Hostname for HomeAssistant
     */
    @WithDefault("homeassistant.local")
    String host();

    /**
     * Port for HomeAssistant
     */
    @WithDefault("8123")
    int port();

    /**
     * Token to use for authenticate against HomeAssistant
     */
    String token();

}
