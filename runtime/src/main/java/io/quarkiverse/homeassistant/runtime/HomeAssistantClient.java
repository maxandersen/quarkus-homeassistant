package io.quarkiverse.homeassistant.runtime;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkiverse.homeassistant.runtime.model.Area;
import io.quarkiverse.homeassistant.runtime.model.Config;
import io.quarkiverse.homeassistant.runtime.model.EntityState;

public interface HomeAssistantClient {

    void connect();

    List<Area> getAreas();

    JsonNode getEntityRegistry();

    List<EntityState> getStates();

    Config getConfig();

    Area createArea(String name);

    Area deleteArea(String id);

    Area renameArea(String id, String newName);
}
