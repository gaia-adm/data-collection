package com.hp.gaia.agent.onprem.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hp.gaia.agent.config.Credentials;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class ConfigUtils {

    private static ObjectMapper mapper;

    private static ObjectMapper getObjectMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                    .enable(SerializationFeature.INDENT_OUTPUT)
                    .enable(JsonParser.Feature.ALLOW_COMMENTS);
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

    public static void writeConfig(File configFile, Object config) {
        try {
            getObjectMapper().writeValue(configFile, config);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write configuration file " + configFile.getName(), e);
        }
    }

    public static void writeCredentialsConfig(File configFile, CredentialsConfig credentialsConfig) {
        try {
            List<Credentials> credentialsList = credentialsConfig.getCredentials();
            if (credentialsList == null) {
                credentialsList = Collections.emptyList();
            }
            getObjectMapper().writeValue(configFile, credentialsList);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write configuration file " + configFile.getName(), e);
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
