package io.quarkiverse.homeassistant.runtime;

public record CallService(String type, String domain, String service, ServiceTarget target, Object service_data) {
}
