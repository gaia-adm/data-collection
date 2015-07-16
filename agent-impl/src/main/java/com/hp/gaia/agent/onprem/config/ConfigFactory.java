package com.hp.gaia.agent.onprem.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hp.gaia.agent.config.Credentials;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigFactory {

    private static ObjectMapper mapper;

    private static ObjectMapper getObjectMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        }
        return mapper;
    }

    public static <T> T readConfig(File configFile, Class<T> configClass) {
        try {
            return getObjectMapper().readValue(configFile, configClass);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse configuration file " + configFile.getName(), e);
        }
    }

    public static CredentialsConfig readCredentialsConfig(File configFile) {
        try {
            TypeReference ref = new TypeReference<List<Credentials>>() {};
            List<Credentials> credentials = getObjectMapper().readValue(configFile, ref);
            return new CredentialsConfig(credentials);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse configuration file " + configFile.getName(), e);
        }
    }
}
