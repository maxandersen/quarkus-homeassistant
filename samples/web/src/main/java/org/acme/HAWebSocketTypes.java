package org.acme;

public interface HAWebSocketTypes {
    public static final String DEVICE_REGISTRY_LIST = "config/device_registry/list";
    public static final String DEVICE_REGISTRY_UPDATE = "config/device_registry/update";
    
    public static final String AREA_REGISTRY_LIST = "config/area_registry/list";
    public static final String AREA_REGISTRY_CREATE = "config/area_registry/create";
    public static final String AREA_REGISTRY_DELETE = "config/area_registry/delete";
    public static final String AREA_REGISTRY_UPDATE = "config/area_registry/update";
    
    public static final String ENTITY_REGISTRY_LIST = "config/entity_registry/list";
    public static final String ENTITY_REGISTRY_GET = "config/entity_registry/get";
    public static final String ENTITY_REGISTRY_UPDATE = "config/entity_registry/update";
    public static final String ENTITY_REGISTRY_REGISTRY = "config/entity_registry/remove";
}