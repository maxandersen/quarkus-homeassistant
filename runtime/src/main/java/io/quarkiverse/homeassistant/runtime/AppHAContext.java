package io.quarkiverse.homeassistant.runtime;

import java.time.Duration;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkiverse.homeassistant.runtime.model.Area;
import io.quarkiverse.homeassistant.runtime.model.Config;
import io.quarkiverse.homeassistant.runtime.model.EntityState;

@ApplicationScoped
public class AppHAContext implements IHAContext {

    private HomeAssistantAPI api;
    private AsyncHomeAssistantClient ws;

    public AppHAContext(@RestClient HomeAssistantAPI api, HomeAssistantWS ws) {
        this.api = api;
        this.ws = ws;
    }

    @Override
    public HomeAssistantAPI getApi() {
        return api;
    }

    public AsyncHomeAssistantClient ws() {
        return ws;
    }

    @Override
    public void callService(String domain, String service, ServiceTarget target, Object data) {
        //  ws.callService(domain, service, target, data);
    }

    @Override
    public HomeAssistantClient blocking(Duration timeout) {
        return new HomeAssistantClient() {
            @Override
            public void connect() {
                ws.connect();
            }

            @Override
            public List<Area> getAreas() {
                return ws.getAreas().await().atMost(timeout);
            }

            @Override
            public JsonNode getEntityRegistry() {
                return ws.getEntityRegistry().await().atMost(timeout);
            }

            @Override
            public List<EntityState> getStates() {
                return ws.getStates().await().atMost(timeout);
            }

            @Override
            public Config getConfig() {
                return ws.getConfig().await().atMost(timeout);
            }

            @Override
            public Area createArea(String name) {
                return ws.createArea(name).await().atMost(timeout);
            }

            @Override
            public Area deleteArea(String id) {
                return ws.deleteArea(id).await().atMost(timeout);
            }

            @Override
            public Area renameArea(String id, String newName) {
                return ws.renameArea(id, newName).await().atMost(timeout);
            }
        };
    }
}
