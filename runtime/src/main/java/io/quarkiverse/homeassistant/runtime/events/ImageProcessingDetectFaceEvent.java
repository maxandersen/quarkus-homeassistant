package io.quarkiverse.homeassistant.runtime.events;

import io.quarkiverse.homeassistant.runtime.events.ImageProcessingDetectFaceEvent.ImageProcessingDetectFaceData;

public class ImageProcessingDetectFaceEvent extends HAChangeEvent<ImageProcessingDetectFaceData> {

    public ImageProcessingDetectFaceEvent() {
        super();
        setEventType("image_processing.detect_face");
    }

    public static class ImageProcessingDetectFaceData {
        private Double confidence;
        private String name;
        private Double age;
        private String gender;
        private String entityId;

        public Double getConfidence() {
            return confidence;
        }

        public void setConfidence(Double confidence) {
            this.confidence = confidence;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getAge() {
            return age;
        }

        public void setAge(Double age) {
            this.age = age;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getEntityId() {
            return entityId;
        }

        public void setEntityId(String entityId) {
            this.entityId = entityId;
        }
    }
}
