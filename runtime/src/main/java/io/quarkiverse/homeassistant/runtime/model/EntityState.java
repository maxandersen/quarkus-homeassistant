package io.quarkiverse.homeassistant.runtime.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public record EntityState(
        @JsonProperty("entity_id") String id,
        @JsonProperty("state") String state,
        @JsonProperty("attributes") Attributes attributes,
        @JsonProperty("last_changed") Instant lastChanged,
        @JsonProperty("last_reported") Instant lastReported,
        @JsonProperty("last_updated") Instant lastUpdated,
        @JsonProperty("context") Context context) {

    public static record Attributes(
            @JsonProperty("editable") Boolean editable,
            @JsonProperty("id") String id,
            @JsonProperty("latitude") Double latitude,
            @JsonProperty("longitude") Double longitude,
            @JsonProperty("gps_accuracy") Double gpsAccuracy,
            @JsonProperty("source") String source,
            @JsonProperty("user_id") String userId,
            @JsonProperty("device_trackers") List<String> deviceTrackers,
            @JsonProperty("entity_picture") String entityPicture,
            @JsonProperty("friendly_name") String friendlyName,
            Map<String, Object> unknown) {

        public Attributes {
            unknown = new HashMap<>();
        }

        @JsonAnySetter
        public void setUnknown(String name, Object value) {
            unknown.put(name, value);
        }
    }

    public static record Context(
            @JsonProperty("id") String id,
            @JsonProperty("parent_id") String parentId,
            @JsonProperty("user_id") String userId) {
    }
}
