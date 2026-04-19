package dk.xam.hassq;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

public class CLIConfigSource implements ConfigSource {
    private static final Map<String, String> configuration = new HashMap<>();

    static {
        String defaultServer = "http://homeassistant.local:8123";
        configuration.put("hass-server", defaultServer);
        configuration.put("quarkus.homeassistant.url", defaultServer);
        configuration.put("quarkus.rest-client.homeassistant-api.url", defaultServer);
    }

    public static void put(String key, String value) {
        configuration.put(key, value);
        // Map CLI args to extension config keys
        if ("hass-server".equals(key)) {
            configuration.put("quarkus.homeassistant.url", value);
            configuration.put("quarkus.rest-client.homeassistant-api.url", value);
        } else if ("hass-token".equals(key)) {
            configuration.put("quarkus.homeassistant.token", value);
        }
    }

    @Override
    public int getOrdinal() {
        return 275;
    }

    @Override
    public Set<String> getPropertyNames() {
        return configuration.keySet();
    }

    @Override
    public String getValue(final String propertyName) {
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return CLIConfigSource.class.getSimpleName();
    }
}