package io.quarkiverse.homeassistant.it;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkiverse.homeassistant.runtime.HAEntity;
import io.quarkiverse.homeassistant.runtime.HassEntity;
import io.quarkiverse.homeassistant.runtime.IHAContext;
import io.quarkiverse.homeassistant.runtime.ServiceTarget;
import io.quarkiverse.homeassistant.runtime.model.EntityState;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class HomeAssistantAPITest {

    @Inject
    IHAContext ha;

    @Inject
    @HassEntity("light.bed_light")
    HAEntity bedLight;

    @BeforeEach
    void setup() {
        ha.ws().connect();
    }

    @Test
    public void testGetState() throws InterruptedException, ExecutionException, TimeoutException {
        assertThat(ha.getApi().getState("sensor.power_consumption").state()).isEqualTo("100");
    }

    @Test
    public void testGetAreas() {
        assertThat(ha.ws().getAreas().await().atMost(Duration.ofSeconds(2))).hasSize(0); // there rare no areas in basic HA :/
    }

    @Test
    public void testGet() {
        assertThat(ha.ws().getEntityRegistry().await().atMost(Duration.ofSeconds(2))).isNotEmpty();
    }

    @Test
    public void testGetStates() {
        assertThat(ha.ws().getStates().await().atMost(Duration.ofSeconds(4))).isNotEmpty();
    }

    @Test
    public void testCallService() {
        // Call turn_on on a demo light entity — should not throw
        ServiceTarget target = new ServiceTarget(List.of("light.bed_light"), null, null);
        assertThatNoException().isThrownBy(
                () -> ha.ws().callService("light", "turn_on", target, null).await().atMost(Duration.ofSeconds(4)));
    }

    @Test
    public void testEntityInjection() {
        assertThat(bedLight).isNotNull();
        assertThat(bedLight.getEntityId()).isEqualTo("light.bed_light");
        assertThat(bedLight.getDomain()).isEqualTo("light");
    }

    @Test
    public void testEntityTurnOn() {
        bedLight.turnOn();
        assertThat(bedLight.getState().state()).isEqualTo("on");
    }

    @Test
    public void testEntityTurnOff() {
        bedLight.turnOff();
        assertThat(bedLight.getState().state()).isEqualTo("off");
    }

    @Test
    public void testStateChanges() throws Exception {
        // Ensure light is off first
        bedLight.turnOff();

        // Subscribe to state changes and capture the next one
        CompletableFuture<EntityState> nextState = new CompletableFuture<>();
        bedLight.stateChanges()
                .subscribe().with(
                        nextState::complete,
                        nextState::completeExceptionally);

        // Trigger a state change
        bedLight.turnOn();

        // Wait for the event
        EntityState state = nextState.get(5, TimeUnit.SECONDS);
        assertThat(state).isNotNull();
        assertThat(state.state()).isEqualTo("on");
    }
}
