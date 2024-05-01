package org.acme;

import io.quarkiverse.homeassistant.runtime.IHAContext;

interface EntityCore {

    IHAContext getContext();

    String getEntityId();

}