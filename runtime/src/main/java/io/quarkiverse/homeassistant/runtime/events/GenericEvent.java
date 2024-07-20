package io.quarkiverse.homeassistant.runtime.events;

import com.fasterxml.jackson.databind.JsonNode;

public class GenericEvent implements HAEvent {

    JsonNode node;

    public GenericEvent(JsonNode jsonNode) {
        this.node = jsonNode;
    }

    public JsonNode getNode() {
        return node;
    }
}
