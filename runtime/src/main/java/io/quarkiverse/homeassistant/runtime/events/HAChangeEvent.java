package io.quarkiverse.homeassistant.runtime.events;

import java.time.Instant;

public class HAChangeEvent<DataType> implements HAEvent {
    private String eventType;
    private DataType data;
    private String origin;
    private Instant timeFired;
    private Context context;

    public HAChangeEvent() {
    }

    public void StateChangeEvent(String eventType, DataType data, String origin, Instant timeFired, Context context) {
        this.eventType = eventType;
        this.data = data;
        this.origin = origin;
        this.timeFired = timeFired;
        this.context = context;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public DataType getData() {
        return data;
    }

    public void setData(DataType data) {
        this.data = data;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Instant getTimeFired() {
        return timeFired;
    }

    public void setTimeFired(Instant timeFired) {
        this.timeFired = timeFired;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static class Context {
        private String id;
        private String parentId;
        private String userId;

        public Context() {
        }

        public Context(String id, String parentId, String userId) {
            this.id = id;
            this.parentId = parentId;
            this.userId = userId;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getParentId() {
            return parentId;
        }

        public void setParentId(String parentId) {
            this.parentId = parentId;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }
    }
}
