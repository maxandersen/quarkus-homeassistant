package org.acme;

import io.quarkiverse.homeassistant.runtime.HomeAssistantAPI;
import io.quarkiverse.homeassistant.runtime.IHAContext;
import io.quarkus.arc.Arc;
import io.quarkus.qute.TemplateGlobal;

@TemplateGlobal
public class WebExtensions {

    public static HomeAssistantAPI hass() {
        return Arc.container().instance(IHAContext.class).get().getApi();
    }
}