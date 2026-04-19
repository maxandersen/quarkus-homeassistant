package io.quarkiverse.homeassistant.runtime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CallService(
        String type,
        String domain,
        String service,
        ServiceTarget target,
        @JsonProperty("service_data") Object serviceData) {
}
