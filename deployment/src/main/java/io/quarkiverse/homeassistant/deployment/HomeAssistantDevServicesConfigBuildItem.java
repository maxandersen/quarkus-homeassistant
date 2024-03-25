package io.quarkiverse.homeassistant.deployment;

import java.util.Map;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * Build item used to carry running values to Dev UI.
 */
public final class HomeAssistantDevServicesConfigBuildItem extends SimpleBuildItem {

    private final Map<String, String> config;

    public HomeAssistantDevServicesConfigBuildItem(Map<String, String> config) {
        this.config = config;
    }

    public Map<String, String> getConfig() {
        return config;
    }

}