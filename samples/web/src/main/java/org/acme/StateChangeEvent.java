package org.acme;

import com.fasterxml.jackson.databind.JsonNode;

public class StateChangeEvent implements HAEvent {


    public JsonNode event;

    public StateChangeEvent(JsonNode event) {
        this.event = event;
    }

}
