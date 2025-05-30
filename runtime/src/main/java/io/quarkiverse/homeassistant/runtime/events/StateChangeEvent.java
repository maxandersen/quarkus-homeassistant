package io.quarkiverse.homeassistant.runtime.events;

import io.quarkiverse.homeassistant.runtime.events.StateChangeEvent.StateChangeData;
import io.quarkiverse.homeassistant.runtime.model.EntityState;

public class StateChangeEvent extends HAChangeEvent<StateChangeData> {

    public StateChangeEvent() {
        super();
        setEventType("image_processing.detect_face");
    }

    static public class StateChangeData {

        private String entityId;
        private EntityState oldState;
        private EntityState newState;

        public StateChangeData() {
        }

        public void EventData(String entityId, EntityState oldState, EntityState newState) {
            this.entityId = entityId;
            this.oldState = oldState;
            this.newState = newState;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }

        public EntityState getOldState() {
            return oldState;
        }

        public void setOldState(EntityState oldState) {
            this.oldState = oldState;
        }

        public EntityState getNewState() {
            return newState;
        }

        public void setNewState(EntityState newState) {
            this.newState = newState;
        }

    }
}
