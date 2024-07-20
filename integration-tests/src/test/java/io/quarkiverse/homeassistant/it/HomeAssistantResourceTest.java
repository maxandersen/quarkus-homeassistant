package io.quarkiverse.homeassistant.it;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class HomeAssistantResourceTest {

    /*
     * @Inject
     * IHAContext ha;
     *
     * CompletableFuture<HAConnected> received = new CompletableFuture<>(); // Declare CompletableFuture
     *
     * HAConnected haconnect;
     */
    /*
     * public void onConnect(@ObservesAsync HAConnected hace) {
     * received.complete(hace); // Complete the future when event is received
     * }
     */

    @Test
    public void testHelloEndpoint() throws InterruptedException, ExecutionException, TimeoutException {
        /*
         * ha.ws().connect();
         * HAConnected event = received.get(5, TimeUnit.SECONDS); // Wait for the future to complete with a timeout of 5 seconds
         * assertNotNull(event); // Assert that the event was received
         */
    }
}
