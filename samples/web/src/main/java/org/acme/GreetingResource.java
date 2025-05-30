package org.acme;

import java.io.IOException;
import java.util.Map;

import io.quarkiverse.homeassistant.runtime.IHAContext;
import io.quarkiverse.homeassistant.runtime.events.GenericEvent;
import io.quarkiverse.homeassistant.runtime.events.HAEvent;
import io.quarkiverse.homeassistant.runtime.events.ImageProcessingDetectFaceEvent;
import io.quarkiverse.homeassistant.runtime.events.StateChangeEvent;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.ObservesAsync;
import jakarta.inject.Inject;
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

    void onStart(@Observes StartupEvent ev) throws IOException {               
        ha.ws().connect();

       // kitchenLightl.stateChanges().when(l -> l.isOn()).do(...)
    }

    void onStateChanged(@ObservesAsync GenericEvent ge) {
        Log.warn("Generic event " + ge);
    }

    void onStateChanged(@ObservesAsync StateChangeEvent ev) {
        Log.warn("state change observed! " + ev.getEventType() + " " + ev.getData());
    }

    void onFaceDetected(@ObservesAsync ImageProcessingDetectFaceEvent ev) {
      Log.warn("face detect observed! " + ev.getEventType() + " " + ev.getData() + " " + ev);
  }

    void onStateChanged(@ObservesAsync HAEvent hev) {
      //  Log.warn("ha event once or twice " + hev.toString());
    }
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() throws IOException {
        
      //  JsonNode areas = ha.ws().sendRequest(Map.of("type", "config/entity_registry/list"))
       // .await().atMost(Duration.ofSeconds(5));

       // return "climate: " + ha.getApi().getState("climate.ecobee") + " -> " + areas.getNodeType().toString();
        return "best";
    }

    @GET
    @Path("/notify")
    @Produces(MediaType.TEXT_PLAIN)
    public String notifi() throws IOException {

        ha.callService("notify", "persistent_notifcation", null, Map.of("message", "notify me", "title", "Hello World!"));

        return "success";
    }
}
