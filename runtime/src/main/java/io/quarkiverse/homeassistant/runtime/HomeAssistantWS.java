package io.quarkiverse.homeassistant.runtime;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkiverse.homeassistant.runtime.events.GenericEvent;
import io.quarkiverse.homeassistant.runtime.events.HAEvent;
import io.quarkiverse.homeassistant.runtime.events.ImageProcessingDetectFaceEvent;
import io.quarkiverse.homeassistant.runtime.events.StateChangeEvent;
import io.quarkiverse.homeassistant.runtime.model.Area;
import io.quarkiverse.homeassistant.runtime.model.Config;
import io.quarkiverse.homeassistant.runtime.model.EntityState;
import io.quarkus.websockets.next.*;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.mutiny.subscription.UniEmitter;

@Singleton
public class HomeAssistantWS implements AsyncHomeAssistantClient {

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
    public static final String WS_TYPE_STATES = "get_states";

    static private final Logger logger = Logger.getLogger(HomeAssistantWS.class);
    private final URI server;
    private final ObjectMapper mapper;
    @Inject
    WebSocketConnector<ClientEndpoint> connector;

    @Inject
    ClientEndpoint cep; // make sure cep have connection to atomiid map to avoid concurrency conflicts

    private WebSocketClientConnection connection;

    public HomeAssistantWS(
            @ConfigProperty(name = "quarkus.homeassistant.url") String server, ObjectMapper mapper) {
        this.mapper = mapper;
        this.server = URI.create(server.replaceFirst("http://", "ws://").replaceFirst("https://", "wss://"));
    }

    @Override
    public void connect() {
        connection = connector.baseUri(server).connectAndAwait();
    }

    /**
     *
     * @param atomicId HomeAssistant API assumes a forever growing id on every sent message this id is then paired to its
     *        relevant responses.
     * @param requestEmitters map from id to those waiting for response of request requests (i.e. getAreas)
     * @param eventEmitters mapped from id to those listen to stream of changes/events
     */
    record HAConnectionState(AtomicInteger atomicId, Map<Integer, UniEmitter<? super JsonNode>> requestEmitters,
            Map<Integer, MultiEmitter<? super JsonNode>> eventEmitters) {
        HAConnectionState() {
            this(new AtomicInteger(1), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        }

        int nextId() {
            return atomicId.getAndIncrement();
        }

    }

    @WebSocketClient(path = "/api/websocket")
    public static class ClientEndpoint {

        @Inject
        ObjectMapper mapper;

        // those wanting to hear about  HAEvent's
        @Inject
        Event<HAEvent> event;

        //TODO: how to give it a context dependent token?
        @ConfigProperty(name = "quarkus.homeassistant.token")
        String accessToken;

        @OnTextMessage
        Uni<Map<String, Object>> onMessage(JsonNode msg, WebSocketClientConnection connection) {

            boolean success = msg.has("success") ? msg.get("success").asBoolean() : true;

            if (!success) {
                logger.error("Error from Home Assistant: " + msg.get("message").asText());
                return null;
            }

            String type = msg.get("type").asText();

            String status = null;

            var state = state(connection);

            try {
                switch (type) {
                    case "auth_required":
                        Map<String, Object> response = Map.of("type", "auth", "access_token", accessToken);
                        status = "Attempting connection";
                        return Uni.createFrom().item(response);
                    case "auth_invalid":
                        logger.error("Unable to authenticate with Home Assistant: " + msg.get("message").asText());
                        status = "Failed to connect: " + msg.get("message").asText();
                        break;
                    case "auth_ok":
                        status = "Successfully connected";
                        subscribeToEvents(connection);
                        break;
                    case "result":
                        int id = msg.get("id").asInt();
                        //todo: just have one map and store value as record { emitter, type = uni or multi }?
                        if (state.requestEmitters.containsKey(id)) {
                            var emitter = state.requestEmitters.remove(id);
                            emitter.complete(msg);
                            status = "Uni Response received";
                        } else if (state.eventEmitters.containsKey(id)) {
                            logger.info("success response for event: " + id); // todo: ignore the ack
                            status = "success Response received for event";
                        } else {
                            logger.warn("Received response with unknown or missing ID: " + msg.toPrettyString());
                            logger.warn("requestEmitters: " + state.requestEmitters);
                        }

                        break;
                    case "event":
                        id = msg.get("id").asInt();
                        var emitter = state.eventEmitters.get(id);
                        if (emitter != null) {
                            emitter.emit(msg);
                        } else {
                            logger.warn("Received event with unknown or missing ID: " + msg.toPrettyString());
                        }
                        status = "Event received";
                        break;

                    default:
                        logger.warn("Unhandled message type: " + msg.toPrettyString());
                }
            } finally {
                if (status == null) {
                    logger.info("status: " + status + " resp: " + msg.toPrettyString());
                }
            }
            return null;
        }

        private HAConnectionState state(WebSocketClientConnection connection) {
            var state = connection.userData().get(new UserData.TypedKey<HAConnectionState>("state"));
            if (state == null) {
                state = new HAConnectionState();
                connection.userData().put(new UserData.TypedKey<HAConnectionState>("state"), state);
            }
            return state;
        }

        /**
         * Subscribe this websocket connection events, listen to them and fire them as events.
         *
         * @param connection
         */
        private void subscribeToEvents(WebSocketClientConnection connection) {
            listenToRequest(Map.of("type", "subscribe_events"), connection).subscribe().with(
                    r -> {
                        if ("result".equals(r.get("type").asText())) {
                            logger.info("Subscribed to events!");
                        } else if ("event".equals(r.get("type").asText())) {
                            String eventType = r.get("event").get("event_type").asText();
                            if (eventType.equals("state_changed")) {
                                try {
                                    var value = mapper.treeToValue(r.get("event"), StateChangeEvent.class);
                                    logger.warn("state changed! json:" + r);
                                    logger.info("state changed! real: " + value);
                                    event.fireAsync(value);
                                } catch (JsonProcessingException | IllegalArgumentException e) {
                                    //todo: handle error?
                                    e.printStackTrace();
                                }
                            } else if (eventType.equals("image_processing.detect_face")) {
                                try {
                                    var value = mapper.treeToValue(r.get("event"), ImageProcessingDetectFaceEvent.class);
                                    logger.warn("image processing event! json:" + r);
                                    logger.info("image processing event! parsed: " + value);
                                    event.fireAsync(value);
                                } catch (JsonProcessingException | IllegalArgumentException e) {
                                    //todo: handle error?
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    logger.warn("generic/unknown event! json:" + r.get("event"));
                                    var value = mapper.treeToValue(r.get("event"), GenericEvent.class);
                                    logger.info("generic/unknown event! parsed: " + value);
                                    event.fireAsync(value);
                                } catch (JsonProcessingException | IllegalArgumentException e) {
                                    logger.error("Could not parse event: " + r.get("event"), e);
                                }
                            }

                        } else {
                            logger.warn("Unkonwn message for event handling " + r);
                        }
                    },
                    failure -> {
                        logger.error("Failure to subscribe to events", failure);
                    });
        }

        public Multi<JsonNode> listenToRequest(Map<String, Object> input, WebSocketClientConnection connection) {
            return Multi.createFrom().emitter(emitter -> {
                var state = state(connection);
                int currentId = state.nextId();
                Map<String, Object> requestData = new HashMap<>(input);
                requestData.put("id", currentId);
                //requestEmitters.put(currentId, emitter);
                state.eventEmitters.put(currentId, emitter);
                try {
                    String jsonRequest = mapper.writeValueAsString(requestData);
                    logger.info("Sending request: " + jsonRequest);
                    connection.sendText(jsonRequest).subscribe()
                            .with(result -> {
                            },
                                    failure -> {
                                        emitter.fail(new IllegalStateException("Failed to send request: " + jsonRequest));
                                    });
                } catch (JsonProcessingException e) {
                    emitter.fail(e);
                }
            });
        }

        public Uni<JsonNode> sendRequest(Object command, WebSocketClientConnection connection) {
            return Uni.createFrom().emitter(emitter -> {
                var state = state(connection);
                int currentId = state.nextId();
                ObjectNode requestData = (ObjectNode) mapper.valueToTree(command);
                requestData.put("id", currentId);
                state.requestEmitters.put(currentId, emitter);

                try {
                    String jsonRequest = mapper.writeValueAsString(requestData);
                    logger.info("Sending request: " + jsonRequest);
                    connection.sendText(jsonRequest).subscribe().with(result -> {
                    }, failure -> {
                        emitter.fail(new IllegalStateException("Failed to send request: " + jsonRequest));
                    });
                } catch (JsonProcessingException e) {
                    emitter.fail(e);
                }
            });
        }

        public <T> Uni<T> sendRequest(WebSocketClientConnection connection, JavaType type, String command) {
            return sendRequest(Map.of("type", command), connection).map(node -> {
                try {
                    return mapper.treeToValue(node.get("result"), type);
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Could not convert " + node + " to " + type);
                }
            });
        }

        public <T> Uni<T> sendRequest(WebSocketClientConnection connection, JavaType type, String command, Object... list) {
            Map map = new HashMap();
            for (int i = 0; i < list.length; i += 2) {
                map.put(list[i], list[i + 1]);
            }
            return sendRequest(connection, type, command, map);
        }

        public <T> Uni<T> sendRequest(WebSocketClientConnection connection, JavaType type, String command,
                Map<String, Object> arguments) {
            Map m = new HashMap();
            m.putAll(arguments);
            m.put("type", command); // here so arguments can't overrule

            return sendRequest(m, connection).map(node -> {
                try {
                    return mapper.treeToValue(node.get("result"), type);
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("Could not convert " + node + " to " + type);
                }
            });
        }
    }

    @Override
    public Uni<List<Area>> getAreas() {
        return cep.sendRequest(connection, mapper.getTypeFactory().constructCollectionType(List.class, Area.class),
                "config/area_registry/list");
    }

    @Override
    public Uni<JsonNode> getEntityRegistry() {
        return cep.sendRequest(Map.of("type", WS_TYPE_ENTITY_REGISTRY_LIST), connection);
    }

    @Override
    public Uni<List<EntityState>> getStates() {
        return cep.sendRequest(connection, mapper.getTypeFactory().constructCollectionType(List.class, EntityState.class),
                "get_states");
    }

    @Override
    public Uni<Config> getConfig() {
        return cep.sendRequest(connection, mapper.getTypeFactory().constructType(Config.class), "system_health/info");
    }

    @Override
    public Uni<Area> createArea(String name) {
        return cep.sendRequest(connection, mapper.getTypeFactory().constructType(Area.class), WS_TYPE_AREA_REGISTRY_CREATE,
                "name", name);
    }

    @Override
    public Uni<Boolean> deleteArea(String id) {
        return cep.sendRequest(connection, mapper.getTypeFactory().constructType(Area.class), WS_TYPE_AREA_REGISTRY_DELETE,
                "area_id", id);
    }

    @Override
    public Uni<Area> renameArea(String id, String newName) {
        return cep.sendRequest(connection, mapper.getTypeFactory().constructType(Area.class), WS_TYPE_AREA_REGISTRY_UPDATE,
                "area_id", id, "name", newName);
    }
}
