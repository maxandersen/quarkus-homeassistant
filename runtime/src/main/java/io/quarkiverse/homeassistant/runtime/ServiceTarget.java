package io.quarkiverse.homeassistant.runtime;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServiceTarget(
        @JsonProperty("entity_id") List<String> entityIds,
        @JsonProperty("area_id") List<String> areaIds,
        @JsonProperty("device_id") List<String> deviceIds) {

    public static ServiceTargetBuilder newServiceTarget() {
        return new ServiceTargetBuilder();
    }

}
