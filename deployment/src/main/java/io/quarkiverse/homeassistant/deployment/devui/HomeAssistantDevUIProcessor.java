package io.quarkiverse.homeassistant.deployment.devui;

import java.util.Map;
import java.util.Optional;

import io.quarkiverse.homeassistant.deployment.HomeAssistantContainer;
import io.quarkiverse.homeassistant.deployment.HomeAssistantDevServicesConfigBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class HomeAssistantDevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createVersion(BuildProducer<CardPageBuildItem> cardPageBuildItemBuildProducer,
            Optional<HomeAssistantDevServicesConfigBuildItem> configProps) {
        if (configProps.isPresent()) {
            Map<String, String> config = configProps.get().getConfig();
            final CardPageBuildItem card = new CardPageBuildItem();

            // UI
            if (config.containsKey(HomeAssistantContainer.CONFIG_HTTP_SERVER)) {
                String uiPath = config.get(HomeAssistantContainer.CONFIG_HTTP_SERVER);
                card.addPage(Page.externalPageBuilder("HomeAssistant UI")
                        .url(uiPath, uiPath)
                        .isHtmlContent()
                        .icon("font-awesome-solid:house"));
            }
        }
    }
}
