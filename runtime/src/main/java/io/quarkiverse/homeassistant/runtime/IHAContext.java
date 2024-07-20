package io.quarkiverse.homeassistant.runtime;

public interface IHAContext {

    HomeAssistantAPI getApi();

    AsyncHomeAssistantClient ws();

    void callService(String domain, String service, ServiceTarget target, Object data);
}
