package io.quarkiverse.homeassistant.runtime;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.quarkiverse.homeassistant.runtime.model.EntityState;
import io.smallrye.mutiny.Multi;

/**
 * A wrapper around a Home Assistant entity that provides convenience methods
 * for interacting with it (turn on/off, get state, call services).
 *
 * <p>
 * Instances are created by the Quarkus extension and injected via CDI
 * using the {@link HassEntity} qualifier.
 */
public class HAEntity {

    private final String entityId;
    private final String domain;
    private IHAContext context;

    public HAEntity(String entityId) {
        this.entityId = entityId;
        int dot = entityId.indexOf('.');
        if (dot <= 0) {
            throw new IllegalArgumentException("Invalid entity ID (expected 'domain.name'): " + entityId);
        }
        this.domain = entityId.substring(0, dot);
    }

    /**
     * Set the HA context. Called by the recorder after CDI initialization.
     */
    public void setContext(IHAContext context) {
        this.context = context;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getDomain() {
        return domain;
    }

    /**
     * Get the current state of this entity via the REST API.
     */
    public EntityState getState() {
        requireContext();
        return context.getApi().getState(entityId);
    }

    /**
     * Turn on this entity.
     */
    public void turnOn() {
        callService("turn_on");
    }

    /**
     * Turn on this entity with additional service data.
     */
    public void turnOn(Map<String, Object> data) {
        callService("turn_on", data);
    }

    /**
     * Turn off this entity.
     */
    public void turnOff() {
        callService("turn_off");
    }

    /**
     * Toggle this entity.
     */
    public void toggle() {
        callService("toggle");
    }

    /**
     * Call any service on this entity's domain.
     */
    public void callService(String service) {
        callService(service, null);
    }

    /**
     * Call any service on this entity's domain with additional service data.
     */
    public void callService(String service, Map<String, Object> data) {
        requireContext();
        ServiceTarget target = new ServiceTarget(List.of(entityId), null, null);
        context.callService(domain, service, target, data);
    }

    /**
     * Returns a reactive stream of state changes for this entity.
     * Filters the global state change stream to only emit events for this entity.
     */
    public Multi<EntityState> stateChanges() {
        requireContext();
        return context.ws().stateChanges()
                .filter(e -> e.getData() != null && entityId.equals(e.getData().getEntityId()))
                .map(e -> e.getData().getNewState());
    }

    /**
     * Register a callback that fires on every state change for this entity.
     * The callback runs on the Vert.x event loop — keep it non-blocking.
     */
    public void onStateChange(Consumer<EntityState> callback) {
        stateChanges().subscribe().with(callback);
    }

    private void requireContext() {
        if (context == null) {
            throw new IllegalStateException("HAEntity not initialized — IHAContext not set for entity: " + entityId);
        }
    }

    @Override
    public String toString() {
        return "HAEntity[" + entityId + "]";
    }
}
