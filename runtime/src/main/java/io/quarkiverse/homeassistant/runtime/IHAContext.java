package io.quarkiverse.homeassistant.runtime;

public interface IHAContext {

    HomeAssistantAPI getApi();

    AsyncHomeAssistantClient ws();

}
