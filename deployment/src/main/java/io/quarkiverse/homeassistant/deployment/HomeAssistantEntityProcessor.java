package io.quarkiverse.homeassistant.deployment;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.logging.Logger;

import io.quarkiverse.homeassistant.runtime.HAEntity;
import io.quarkiverse.homeassistant.runtime.HassEntity;
import io.quarkiverse.homeassistant.runtime.HomeAssistantRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;

/**
 * Scans for {@link HassEntity} injection points and produces synthetic CDI beans
 * for each unique entity ID.
 */
public class HomeAssistantEntityProcessor {

    private static final Logger log = Logger.getLogger(HomeAssistantEntityProcessor.class);
    private static final DotName HASS_ENTITY = DotName.createSimple(HassEntity.class.getName());

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerEntityBeans(
            CombinedIndexBuildItem combinedIndex,
            HomeAssistantRecorder recorder,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {

        Set<String> entityIds = new HashSet<>();

        for (AnnotationInstance annotation : combinedIndex.getIndex().getAnnotations(HASS_ENTITY)) {
            String entityId = annotation.value().asString();
            entityIds.add(entityId);
        }

        for (String entityId : entityIds) {
            log.infof("Registering HAEntity bean for: %s", entityId);

            syntheticBeans.produce(SyntheticBeanBuildItem.configure(HAEntity.class)
                    .scope(ApplicationScoped.class)
                    .addQualifier().annotation(HassEntity.class).addValue("value", entityId).done()
                    .supplier(recorder.createEntity(entityId))
                    .setRuntimeInit()
                    .unremovable()
                    .done());
        }
    }
}
