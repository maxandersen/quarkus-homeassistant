package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.jackson.ObjectMapperCustomizer;
import jakarta.inject.Singleton;

@Singleton
public class RegisterCustomModuleCustomizer implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mapper.setPropertyNamingStrategy(com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE);
    }
}