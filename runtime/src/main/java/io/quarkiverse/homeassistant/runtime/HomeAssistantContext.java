package io.quarkiverse.homeassistant.runtime;

import org.eclipse.microprofile.rest.client.inject.RestClient;

public class HomeAssistantContext implements IHAContext {

    private HomeAssistantAPI api;

    public HomeAssistantContext(@RestClient HomeAssistantAPI api) {
        this.api = api;
    }

    @Override
    public HomeAssistantAPI getApi() {
        return api;
    }
}
