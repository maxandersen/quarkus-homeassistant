package io.quarkiverse.homeassistant.runtime;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkiverse.homeassistant.runtime.events.HAEvent;
import io.quarkiverse.homeassistant.runtime.events.StateChangeEvent;
import io.quarkiverse.homeassistant.runtime.model.Area;
import io.quarkiverse.homeassistant.runtime.model.Config;
import io.quarkiverse.homeassistant.runtime.model.Entity;
import io.quarkiverse.homeassistant.runtime.model.EntityState;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface AsyncHomeAssistantClient {

    void connect();

    Uni<List<Area>> getAreas();

    Uni<JsonNode> getEntityRegistry();

    Uni<List<EntityState>> getStates();

    Uni<Config> getConfig();

    Uni<Area> createArea(String name);

    Uni<Boolean> deleteArea(String id);

    Uni<Area> renameArea(String id, String newName);

    Multi<HAEvent> subscribeToEvents(String event);

    Uni<List<Entity>> getEntities();

    Uni<Void> callService(String domain, String service, ServiceTarget target, Object data);

    /**
     * Returns a broadcast Multi of all state change events from the WebSocket connection.
     */
    Multi<StateChangeEvent> stateChanges();

}
