package io.quarkiverse.homeassistant.runtime;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

@ApplicationScoped
public class AppHAContext implements IHAContext {

    private HomeAssistantAPI api;
    private HomeAssistantWS ws;

    public AppHAContext(@RestClient HomeAssistantAPI api, HomeAssistantWS ws) {
        this.api = api;
        this.ws = ws;
    }

    @Override
    public HomeAssistantAPI getApi() {
        return api;
    }

    public HomeAssistantWS ws() {
        return ws;
    }
}
