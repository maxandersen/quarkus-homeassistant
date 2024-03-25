package io.quarkiverse.homeassistant.deployment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import io.quarkus.devservices.common.ConfigureUtil;

/**
 * Testcontainers implementation for HomeAssistant server.
 * <p>
 * Supported image: {@code ghcr.io/maxandersen/private-demo}
 * <p>
 * Exposed ports: 8123 (http user interface)
 */
public final class HomeAssistantContainer extends GenericContainer<HomeAssistantContainer> {

    public static final String CONFIG_HTTP_SERVER = HomeAssistantProcessor.FEATURE + ".http.server";

    /**
     * Logger which will be used to capture container STDOUT and STDERR.
     */
    private static final Logger log = Logger.getLogger(HomeAssistantContainer.class);

    /**
     * Default HomeAssistant HTTP Port for UI.
     */
    private static final Integer PORT_HTTP = 8123;

    /**
     * Flag whether to use shared networking
     */
    private final boolean useSharedNetwork;

    /**
     * The dynamic host name determined from TestContainers.
     */
    private String hostName;
    private IndexView index;

    HomeAssistantContainer(HomeAssistantConfig config, boolean useSharedNetwork, IndexView index) {
        super(DockerImageName.parse(config.imageName()).asCompatibleSubstituteFor(HomeAssistantConfig.DEFAULT_IMAGE));
        this.useSharedNetwork = useSharedNetwork;
        this.index = index;

        super.withLabel(HomeAssistantProcessor.DEV_SERVICE_LABEL, HomeAssistantProcessor.FEATURE);
        super.withNetwork(Network.SHARED);
        super.waitingFor(Wait.forHttp("/").forPort(PORT_HTTP));

        // configure verbose container logging
        // if (config.verbose()) {
        //     super.withEnv("MP_VERBOSE", "true");
        // }

        // forward the container logs
        //super.withLogConsumer(new JbossContainerLogConsumer(log).withPrefix(HomeassistantProcessor.FEATURE));
    }

    @Override
    protected void configure() {
        super.configure();

        if (useSharedNetwork) {
            hostName = ConfigureUtil.configureSharedNetwork(this, HomeAssistantProcessor.FEATURE);
            return;
        }

        addExposedPorts(PORT_HTTP);
    }

    /**
     * Info about the DevService used in the DevUI.
     *
     * @return the map of as running configuration of the dev service
     */
    public Map<String, String> getExposedConfig() {
        Map<String, String> exposed = new HashMap<>();

        final String port = Objects.toString(getMappedPort(PORT_HTTP));

        exposed.put(CONFIG_HTTP_SERVER, getHomeAssistantHttpServer());
        exposed.putAll(super.getEnvMap());

        // quarkus mailer default
        exposed.put("quarkus.homeassistant.port", port);
        exposed.put("quarkus.homeassistant.host", getHost());
        // exposed.put("quarkus.mailer.mock", "false");

        return exposed;
    }

    /**
     * Get the calculated HomeAssistant UI location for use in the DevUI.
     *
     * @return the calculated full URL to the homeassistant UI
     */
    public String getHomeAssistantHttpServer() {
        return String.format("http://%s:%d", getHost(), getMappedPort(PORT_HTTP));
    }
}
