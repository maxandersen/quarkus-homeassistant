package io.quarkiverse.homeassistant.runtime;

import java.util.List;

public record ServiceTarget(List<String> entityIds, List<String> areaIds, List<String> deviceIds) {

    public static ServiceTargetBuilder newServiceTarget() {
        return new ServiceTargetBuilder();
    }

}
