package io.quarkiverse.homeassistant.runtime.config;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigRoot(phase = ConfigPhase.RUN_TIME)
@ConfigMapping(prefix = "quarkus.homeassistant")
public interface HomeAssistantConfig {

    /**
     * URL for HomeAssistant
     */
    @WithDefault("http://homeassistant.local:8123")
    String url();

    /**
     * Token to use for authenticate against HomeAssistant
     */
    Optional<String> token();

}
