package io.quarkiverse.homeassistant.runtime;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.SendHandler;
import jakarta.websocket.SendResult;
import jakarta.websocket.Session;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkiverse.homeassistant.runtime.events.GenericEvent;
import io.quarkiverse.homeassistant.runtime.events.HAEvent;
import io.quarkiverse.homeassistant.runtime.events.StateChangeEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.MultiEmitter;
import io.smallrye.mutiny.subscription.UniEmitter;

@ClientEndpoint
public class HomeAssistantWS implements AsyncHomeAssistantClient {

    private final Logger logger = Logger.getLogger(HomeAssistantWS.class);

    private final String accessToken;
    private String server;
    private ObjectMapper mapper;

    private String status = "";

    // mapped from id to those waiting for response of request
    private Map<Integer, UniEmitter<? super JsonNode>> requestEmitters = new ConcurrentHashMap<>();

    // mapped from id to those listen to stream of events
    private Map<Integer, MultiEmitter<? super JsonNode>> eventEmitters = new ConcurrentHashMap<>();

    @Inject
    Event<HAEvent> event;

    AtomicInteger atomicId = new AtomicInteger(1);

    int nextId() {
        return atomicId.getAndIncrement();
    }

    SendHandler sh = new SendHandler() {

        @Override
        public void onResult(SendResult result) {
            if (!result.isOK()) {
                logger.warn("Error on results", result.getException());
            }
        }

    };

    private Session connection;

    public HomeAssistantWS(ObjectMapper mapper, @ConfigProperty(name = "quarkus.homeassistant.token") String token,
            @ConfigProperty(name = "quarkus.homeassistant.url") String server) {
        this.mapper = mapper;
        this.accessToken = token;
        this.server = server.replaceFirst("http://", "ws://").replaceFirst("https://", "wss://");

    }

    @OnOpen
    public void onOpen(Session session) {
        logger.info("Opening socket to Home Assistant " + server);
    }

    @OnMessage
    public void onMessage(String message, Session session) throws JsonMappingException, JsonProcessingException {
        final JsonNode response = mapper.readTree(message);
        //logger.info(response);
        String type = response.get("type").asText();
        switch (type) {
            case "auth_required":
                sendAuth(session);
                status = "Attempting connection";
                break;
            case "auth_invalid":
                logger.error("Unable to authenticate with Home Assistant: " + response.get("message").asText());
                status = "Failed to connect: " + response.get("message").asText();
                break;
            case "auth_ok":
                status = "Successfully connected";
                listenToEvents();
                break;
            case "result":
                int id = response.get("id").asInt();
                if (requestEmitters.containsKey(id)) {
                    var emitter = requestEmitters.remove(id);
                    emitter.complete(response);
                } else {
                    logger.warn("Received response with unknown or missing ID: " + response.toPrettyString());
                }
                break;
            case "event":
                id = response.get("id").asInt();
                var emitter = eventEmitters.get(id);
                if (emitter != null) {
                    emitter.emit(response);
                } else {
                    logger.warn("Received event with unknown or missing ID: " + response.toPrettyString());
                }
                break;

            default:
                logger.warn("Unhandled message type: " + response.toPrettyString());
        }
        //logger.info("status: " + status + " resp: " + response.toPrettyString());
    }

    private void listenToEvents() {
        listenToRequest(Map.of("type", "subscribe_events"
        /* "event_type", "state_changed" */)).subscribe().with(
                r -> {
                    if ("result".equals(r.get("type").asText())) {
                        logger.info("Subscribed to events!");
                    } else if ("event".equals(r.get("type").asText())) {

                        String eventType = r.get("event").get("event_type").asText();
                        if (eventType.equals("state_changed")) {
                            JsonNode entity_new_state = r.get("event");
                            if (event == null)
                                throw new IllegalStateException("why is event null?");
                            event.fireAsync(new StateChangeEvent(entity_new_state));
                            logger.info("event " + r.toPrettyString());
                        } else {
                            event.fireAsync(new GenericEvent(r.get("event")));
                        }

                    } else {
                        logger.warn("Unkonwn message for event handling " + r);
                    }
                },
                failure -> {
                    logger.error("Failure to subscribe to events", failure);
                });
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        status = "Connection closed by " + reason.toString();
        logger.info(status);
    }

    @OnError
    public void onError(Session s, Throwable ex) {
        status = "Error: " + ex.getMessage();
        logger.warn("error", ex);
    }

    public void sendAuth(Session session) {
        Map<String, Object> response = Map.of("type", "auth", "access_token", accessToken);
        reply(session, response);
    }

    private void reply(Session session, Map<String, Object> response) {
        String json;
        try {
            json = mapper.writeValueAsString(response);
            logger.info("send " + json);
            session.getAsyncRemote().sendText(json, sh);
        } catch (JsonProcessingException e) {
            logger.warn("error while replying", e);
            throw new IllegalStateException(e);
        }
    }

    public void getStates(Session session) {
        reply(session, Map.of("id", nextId(), "type", "get_states"));
    }

    /*
     * public void callService(String domain, String service, JsonObject data,
     * JsonObject target) {
     * JsonObject obj = new JsonObject();
     * obj.add("id", new JsonPrimitive(nextId()));
     * obj.add("type", new JsonPrimitive("call_service"));
     * obj.add("domain", new JsonPrimitive(domain));
     * obj.add("service", new JsonPrimitive(service));
     * obj.add("service_data", data);
     * obj.add("target", target);
     * send(gson.toJson(obj));
     * }
     */

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void connect() {
        String endpoint = server + "/api/websocket";
        logger.warn("connect to " + endpoint);
        try {
            connection = ContainerProvider.getWebSocketContainer().connectToServer(this,
                    URI.create(endpoint));
        } catch (DeploymentException | IOException e) {

            logger.error("Could not connect", e);
        }

    }

    public Multi<JsonNode> listenToRequest(Map<String, Object> input) {
        return Multi.createFrom().emitter(emitter -> {
            int currentId = nextId();
            Map<String, Object> requestData = new HashMap<>(input);
            requestData.put("id", currentId);
            eventEmitters.put(currentId, emitter);
            try {
                String jsonRequest = mapper.writeValueAsString(requestData);
                logger.info("Sending request: " + jsonRequest);
                connection.getAsyncRemote().sendText(jsonRequest, result -> {
                    if (!result.isOK()) {
                        emitter.fail(new IllegalStateException("Failed to send request: " + jsonRequest));
                    }
                    // Don't complete the emitter here; it will be completed when the response is
                    // received
                });
            } catch (JsonProcessingException e) {
                emitter.fail(e);
            }
        });
    }

    public Uni<JsonNode> sendRequest(Object command) {
        return Uni.createFrom().emitter(emitter -> {
            int currentId = nextId();
            ObjectNode requestData = (ObjectNode) mapper.valueToTree(command);
            requestData.put("id", currentId);
            requestEmitters.put(currentId, emitter);

            try {
                String jsonRequest = mapper.writeValueAsString(requestData);
                logger.info("Sending request: " + jsonRequest);
                connection.getAsyncRemote().sendText(jsonRequest, result -> {
                    if (!result.isOK()) {
                        emitter.fail(new IllegalStateException("Failed to send request: " + jsonRequest));
                    }
                    // Don't complete the emitter here; it will be completed when the response is
                    // received
                });
            } catch (JsonProcessingException e) {
                emitter.fail(e);
            }
        });
    }

    public void callService(String domain, String service, ServiceTarget target, Object data) {

        var call = (ObjectNode) mapper.valueToTree(new CallService("call_service", domain, service, target, data));

        sendRequest(call).await().indefinitely();

    }

}
