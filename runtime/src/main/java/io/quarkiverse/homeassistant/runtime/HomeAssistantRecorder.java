package io.quarkiverse.homeassistant.runtime;

import java.util.function.Supplier;

import io.quarkus.arc.Arc;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class HomeAssistantRecorder {

    public Supplier<HAEntity> createEntity(String entityId) {
        return () -> {
            HAEntity entity = new HAEntity(entityId);
            IHAContext context = Arc.container().instance(IHAContext.class).get();
            entity.setContext(context);
            return entity;
        };
    }
}
