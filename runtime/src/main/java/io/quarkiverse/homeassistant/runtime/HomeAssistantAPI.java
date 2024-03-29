package io.quarkiverse.homeassistant.runtime;

import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import com.fasterxml.jackson.databind.JsonNode;

import io.quarkiverse.homeassistant.runtime.model.Config;
import io.quarkiverse.homeassistant.runtime.model.DomainInfo;
import io.quarkiverse.homeassistant.runtime.model.EntityState;
import io.quarkiverse.homeassistant.runtime.model.EventInfo;
import io.quarkus.rest.client.reactive.ClientExceptionMapper;

@Path("/api")
@ClientHeaderParam(name = "Authorization", value = "Bearer ${quarkus.homeassistant.token}")
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient
public interface HomeAssistantAPI {

    @GET
    @Path("/")
    Map<String, String> status();

    @GET
    @Path("config")
    Config getConfig();

    @GET
    @Path("events")
    Set<EventInfo> getEvents();

    @GET
    @Path("services")
    List<DomainInfo> getServices();

    @GET
    @Path("states")
    List<EntityState> getStates();

    @GET
    @Path("states/{entity}")
    EntityState getState(String entity);

    @GET
    @Path("error_log")
    String getErrorLog();

    @GET
    @Path("api/camera_proxy/{entity}")
    String getCameraProxy(String entity);

    @DELETE
    @Path("states/{entity}")
    Response deleteState(String entity);

    @POST
    @Path("states/{entity}")
    void updateState(String entity, JsonNode result);

    @ClientExceptionMapper
    static RuntimeException map(Response response) {
        if (response.getStatus() == 404) {
            Map m = response.readEntity(Map.class);
            if (m.containsKey("message")) {
                //TODO: how do I get the exception that would have happened here so i can chain it?
                return new IllegalStateException("404 - " + m.get("message"));
            }
        }
        return null;
    }

}
