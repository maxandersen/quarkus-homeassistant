import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class NoTokenTestProfile implements QuarkusTestProfile {
    
    @Override
    public Map<String, String> getConfigOverrides() {
        return Map.of("hass-token", "");
    }
}