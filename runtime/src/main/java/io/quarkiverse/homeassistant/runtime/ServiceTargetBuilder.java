package io.quarkiverse.homeassistant.runtime;

import java.util.List;

public class ServiceTargetBuilder {
    private List<String> entityIds;
    private List<String> areaIds;
    private List<String> deviceIds;

    public ServiceTargetBuilder() {
    }

    public ServiceTargetBuilder entityIds(List<String> entityIds) {
        this.entityIds = entityIds;
        return this;
    }

    public ServiceTargetBuilder areaIds(List<String> areaIds) {
        this.areaIds = areaIds;
        return this;
    }

    public ServiceTargetBuilder deviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
        return this;
    }

    public ServiceTarget build() {
        return new ServiceTarget(entityIds, areaIds, deviceIds);
    }
}