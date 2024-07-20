package dk.xam.hassq;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import io.quarkus.logging.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkiverse.homeassistant.runtime.model.Area;
import io.quarkiverse.homeassistant.runtime.model.Entity;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.eclipse.microprofile.config.inject.ConfigProperty;

// super dumb websocket client to HA that just does the bare minimum to
// authenticate and query values.
@ClientEndpoint
public class HomeAssistantWS {

    public static final String WS_TYPE_DEVICE_REGISTRY_LIST = "config/device_registry/list";
    public static final String WS_TYPE_AREA_REGISTRY_LIST = "config/area_registry/list";
    public static final String WS_TYPE_AREA_REGISTRY_CREATE = "config/area_registry/create";
    public static final String WS_TYPE_AREA_REGISTRY_DELETE = "config/area_registry/delete";
    public static final String WS_TYPE_AREA_REGISTRY_UPDATE = "config/area_registry/update";
    public static final String WS_TYPE_DEVICE_REGISTRY_UPDATE = "config/device_registry/update";
    public static final String WS_TYPE_ENTITY_REGISTRY_LIST = "config/entity_registry/list";
    public static final String WS_TYPE_ENTITY_REGISTRY_GET = "config/entity_registry/get";
    public static final String WS_TYPE_ENTITY_REGISTRY_UPDATE = "config/entity_registry/update";
    public static final String WS_TYPE_ENTITY_REGISTRY_REGISTRY = "config/entity_registry/remove";

    private CompletableFuture<String> messageFuture;

    State state;
    int id = 0;

    ObjectMapper mapper;

    String token;

    String server;
    private Consumer<String> callback;

    enum State {
        INIT, AUTHENTICATING, READY, DONE
    }

    public HomeAssistantWS(ObjectMapper mapper, @ConfigProperty(name = "hass-token") String token,
            @ConfigProperty(name = "hass-server") String server) {
        this.mapper = mapper;
        this.token = token;
        this.server = server.replaceFirst("http://", "ws://").replaceFirst("https://", "wss://");

    }

    // todo: drop need for jackson specific type
    @SuppressWarnings("unchecked")
    public <T> T query(Map<String, String> frame, JavaType resultType) {
        String endPoint = server + "/api/websocket";

        Log.debug("Querying " + endPoint + ": " + frame);
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(this,
                URI.create(endPoint))) {

            String message = null;
            while (state != HomeAssistantWS.State.DONE) {
                Log.debug("State: " + state);
                if (state != HomeAssistantWS.State.READY) {
                    message = waitForMessage();
                    T result = handleMessage(message, session, resultType);
                    if (result != null)
                        return result;
                } else {
                    var query = new HashMap<String, Object>();
                    query.put("id", ++id);
                    query.putAll(frame);
                    String json = mapper.writeValueAsString(query);
                    Log.debug("trying to get answer to this query: " + json);
                    session.getAsyncRemote().sendText(json);
                    String last = waitForMessage();
                    var result = handleMessage(last, session, resultType);
                    if (result != null) {
                        state = HomeAssistantWS.State.DONE;
                        return (T) result;
                    } /*
                       * else {
                       * throw new IllegalStateException("Unexpected message: " + last);
                       * }
                       */
                }
            }
        } catch (DeploymentException | IOException | InterruptedException | ExecutionException
                | TimeoutException e) {
            throw new IllegalStateException(e);
        }
        Log.debug("State end: " + state);
        return null;
    }

    @OnOpen
    public void onOpen(Session session) {
        state = State.INIT;
        messageFuture = new CompletableFuture<>();
    }

    public String waitForMessage() throws InterruptedException, ExecutionException, TimeoutException {
        Log.debug("Waiting for message in " + state);
        return messageFuture.get(30, TimeUnit.SECONDS); // Timeout after 30 seconds
    }

    // Reset the future to wait for the next message
    public void resetFuture() {
        messageFuture = new CompletableFuture<>();
    }

    <T> T handleMessage(String msg, Session session, JavaType resultType) throws JsonProcessingException {
        Log.debug("Handling message: " + msg);
        resetFuture();
        var map = mapper.readTree(msg);
        String type = map.get("type").asText();
        switch (state) {
            case AUTHENTICATING:
                if ("auth_ok".equals(type)) {
                    state = State.READY;
                    Log.debug("Authenticated");
                } else if ("auth_invalid".equals(type)) {
                    throw new IllegalStateException("Authentication failed");
                } else {
                    throw new IllegalStateException("Unexpected message: " + msg);
                }
                break;
            case INIT:
                if ("auth_required".equals(type)) {
                    state = State.AUTHENTICATING;
                    var response = Map.of("type", "auth", "access_token", token);
                    String json = mapper.writeValueAsString(response);
                    Log.debug("Trying to authenticate: " + json);
                    session.getAsyncRemote().sendText(json);
                } else {
                    throw new IllegalStateException("Unexpected message: " + msg);
                }
                break;
            case READY:
                if (Boolean.FALSE.equals(map.get("success").asBoolean())) {
                    String code = map.get("error").get("code").asText();
                    String message = map.get("error").get("message").asText();
                    throw new IllegalStateException(code + ": " + message);
                } else if ("result".equals(type)) {
                    state = State.DONE;
                    Log.debug("converting " + map.get("result") + " to " + resultType);
                    return mapper.treeToValue(map.get("result"), resultType);
                } else if("event".equals(type) && callback != null) {
                    callback.accept(msg);
                } else if ("pong".equals(type)) {
                    session.getAsyncRemote().sendText("{\"id\": " + map.get("id") + "\"type\": \"pong\"}");
                } else {
                    throw new IllegalStateException("Unexpected message: " + msg);
                }
            case DONE:
                if("event".equals(type) && callback != null) {
                    callback.accept(msg);
                } else if ("pong".equals(type)) {
                    session.getAsyncRemote().sendText("{\"id\": " + map.get("id") + "\"type\": \"pong\"}");
                }
                return null; // todo: should we throw an exception here?
            default:
                break;
        }
        return null;
    }

    @OnMessage
    void message(String msg, Session session) throws IllegalArgumentException, IOException {
        Log.debug("Received message: " + msg);
        messageFuture.complete(msg);
    }

    public List<Area> getAreas() {
        return query(Map.of("type", "config/area_registry/list"),
                mapper.getTypeFactory().constructCollectionType(List.class, Area.class));
    }

    public String getConfig() {
        return query(Map.of("type", "system_health/info"), mapper.getTypeFactory().constructType(String.class));
    }

    public Area createArea(String name) {
        return query(Map.of("type", WS_TYPE_AREA_REGISTRY_CREATE, "name", name),
                mapper.getTypeFactory().constructType(Area.class));
    }

    public Area deleteArea(String id) {
        return query(Map.of("type", WS_TYPE_AREA_REGISTRY_DELETE, "area_id", id),
                mapper.getTypeFactory().constructType(Area.class));
    }

    public Area renameArea(String id, String newName) {
        return query(Map.of("type", WS_TYPE_AREA_REGISTRY_UPDATE, "area_id", id, "name", newName),
                mapper.getTypeFactory().constructType(Area.class));
    }

    public String watch(String event, java.util.function.Consumer<String> callback) {
        this.callback = callback;
        if(event!=null) {
            return query(Map.of("type", "subscribe_events", "event_type", event), mapper.getTypeFactory().constructType(String.class));
        } else {
            return query(Map.of("type", "subscribe_events"), mapper.getTypeFactory().constructType(String.class));
        }
    }

    public List<Entity> getEntities() {
        return query(Map.of("type", "config/entity_registry/list"),
                mapper.getTypeFactory().constructCollectionType(List.class, Entity.class));
    }


}