package org.acme;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;


import com.fasterxml.jackson.databind.JsonNode;

import io.quarkiverse.homeassistant.runtime.IHAContext;
import io.quarkiverse.homeassistant.runtime.events.GenericEvent;
import io.quarkiverse.homeassistant.runtime.events.HAEvent;
import io.quarkiverse.homeassistant.runtime.events.StateChangeEvent;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
import jakarta.websocket.DeploymentException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
@ApplicationScoped
public class GreetingResource {

    @Inject
    IHAContext ha;

   // @Entity("light.kitchen")
   // Light kitchenLightl;

    void onStart(@Observes StartupEvent ev) throws DeploymentException, IOException {               
        ha.ws().connect();   
       // kitchenLightl.stateChanges().when(l -> l.isOn()).do(...)
    }

    void onStateChanged(@ObservesAsync GenericEvent ge) {
        Log.warn("Generic event " + ge.toString());
    }

    void onStateChanged(@ObservesAsync StateChangeEvent ev) {
        Log.warn("state changed! " + ev.event.toPrettyString());
    }

    void onStateChanged(@ObservesAsync HAEvent hev) {
        Log.warn("ha event once or twice " + hev.toString());
    }
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws DeploymentException, IOException {
        
        JsonNode areas = ha.ws().sendRequest(Map.of("type", "config/entity_registry/list"))
        .await().atMost(Duration.ofSeconds(5));

        return "climate: " + ha.getApi().getState("climate.ecobee") + " -> " + areas.getNodeType().toString();
    }
}
