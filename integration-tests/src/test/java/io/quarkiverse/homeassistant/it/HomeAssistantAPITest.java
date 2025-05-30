package io.quarkiverse.homeassistant.it;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import jakarta.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkiverse.homeassistant.runtime.IHAContext;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class HomeAssistantAPITest {

    @Inject
    IHAContext ha;

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
        assertThat(ha.ws().getEntityRegistry().await().atMost(Duration.ofSeconds(2))).hasSize(32);
    }

    @Test
    public void testGetStates() {
        assertThat(ha.ws().getStates().await().atMost(Duration.ofSeconds(2))).hasSize(88);
    }
}
