package io.quarkiverse.homeassistant.runtime;

import java.time.Duration;

public interface IHAContext {

    HomeAssistantAPI getApi();

    AsyncHomeAssistantClient ws();

    void callService(String domain, String service, ServiceTarget target, Object data);

    HomeAssistantClient blocking(Duration timeout);
}
