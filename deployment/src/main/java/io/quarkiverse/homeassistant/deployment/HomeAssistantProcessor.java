package io.quarkiverse.homeassistant.deployment;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import io.quarkiverse.homeassistant.runtime.AppHAContext;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.bootstrap.classloading.QuarkusClassLoader;
import io.quarkus.deployment.IsNormal;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.DevServicesResultBuildItem;
import io.quarkus.deployment.builditem.DevServicesSharedNetworkBuildItem;
import io.quarkus.deployment.builditem.DockerStatusBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.console.ConsoleInstalledBuildItem;
import io.quarkus.deployment.dev.devservices.GlobalDevServicesConfig;
import io.quarkus.deployment.logging.LoggingSetupBuildItem;

/**
 * Starts a homeassistant server as dev service if needed.
 */
@BuildSteps(onlyIfNot = IsNormal.class, onlyIf = GlobalDevServicesConfig.Enabled.class)
public class HomeAssistantProcessor {

    private static final Logger log = Logger.getLogger(HomeAssistantProcessor.class);

    public static final String FEATURE = "homeassistant";

    /**
     * Label to add to shared Dev Service for homeassistant running in containers.
     * This allows other applications to discover the running service and use it instead of starting a new instance.
     */
    static final String DEV_SERVICE_LABEL = "quarkus-dev-service-homeassistant";

    static volatile DevServicesResultBuildItem.RunningDevService devService;
    static volatile HomeAssistantConfig cfg;
    static volatile boolean first = true;

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    AdditionalBeanBuildItem additionalBeans() {
        return AdditionalBeanBuildItem.builder()
                .setDefaultScope(DotNames.APPLICATION_SCOPED)
                .addBeanClass(AppHAContext.class)
                .build();
    }

    @BuildStep
    public DevServicesResultBuildItem startHomeAssistantDevService(
            DockerStatusBuildItem dockerStatusBuildItem,
            LaunchModeBuildItem launchMode,
            HomeAssistantConfig homeassistantConfig,
            Optional<ConsoleInstalledBuildItem> consoleInstalledBuildItem,
            LoggingSetupBuildItem loggingSetupBuildItem,
            GlobalDevServicesConfig devServicesConfig,
            List<DevServicesSharedNetworkBuildItem> devServicesSharedNetworkBuildItem,
            BuildProducer<HomeAssistantDevServicesConfigBuildItem> homeassistantBuildItemBuildProducer,
            CombinedIndexBuildItem combinedIndexBuildItem) {
        if (devService != null) {
            boolean shouldShutdownTheBroker = !HomeAssistantConfig.isEqual(cfg, homeassistantConfig);
            if (!shouldShutdownTheBroker) {
                return devService.toBuildItem();
            }
            shutdown();
            cfg = null;
        }

        devService = starthomeassistant(dockerStatusBuildItem, homeassistantConfig, devServicesConfig,
                !devServicesSharedNetworkBuildItem.isEmpty(), combinedIndexBuildItem.getIndex());

        if (devService.isOwner()) {
            log.info("Dev Services for homeassistant started.");
            homeassistantBuildItemBuildProducer.produce(new HomeAssistantDevServicesConfigBuildItem(devService.getConfig()));
        }

        // Configure the watch dog
        if (first) {
            first = false;
            Runnable closeTask = () -> {
                if (devService != null) {
                    shutdown();

                    log.info("Dev Services for homeassistant shut down.");
                }
                first = true;
                devService = null;
                cfg = null;
            };
            QuarkusClassLoader cl = (QuarkusClassLoader) Thread.currentThread().getContextClassLoader();
            ((QuarkusClassLoader) cl.parent()).addCloseTask(closeTask);
        }
        cfg = homeassistantConfig;
        return devService.toBuildItem();
    }

    private DevServicesResultBuildItem.RunningDevService starthomeassistant(DockerStatusBuildItem dockerStatusBuildItem,
            HomeAssistantConfig homeassistantConfig, GlobalDevServicesConfig devServicesConfig, boolean useSharedNetwork,
            IndexView index) {

        if (!homeassistantConfig.enabled()) {
            // explicitly disabled
            log.warn("Not starting dev services for HomeAssistant, as it has been disabled in the config.");
            return null;
        }

        if (userConfigured()) {
            log.debug("Not starting dev services for HomeAssistant, as the quarkus.homeassistant.token is configured.");
            return null;
        }

        if (!dockerStatusBuildItem.isContainerRuntimeAvailable()) {
            log.warn("Docker isn't working, not starting dev services for HomeAssistant.");
            return null;
        }

        final HomeAssistantContainer homeassistant = new HomeAssistantContainer(homeassistantConfig, useSharedNetwork, index);
        devServicesConfig.timeout.ifPresent(homeassistant::withStartupTimeout);
        homeassistant.start();

        return new DevServicesResultBuildItem.RunningDevService(FEATURE,
                homeassistant.getContainerId(),
                homeassistant::close,
                homeassistant.getExposedConfig());
    }

    private boolean userConfigured() {
        Config config = ConfigProvider.getConfig();
        Optional<String> token = config.getOptionalValue("quarkus.homeassistant.token", String.class);

        if (token.isPresent()) {
            return true;
        }
        return false;
    }

    private void shutdown() {
        if (devService != null) {
            try {
                log.info("Dev Services for homeassistant shutting down...");
                devService.close();
            } catch (Throwable e) {
                log.error("Failed to stop the homeassistant server", e);
            } finally {
                devService = null;
            }
        }
    }

}
