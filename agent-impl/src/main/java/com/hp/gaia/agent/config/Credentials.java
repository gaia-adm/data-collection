package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

public class Credentials {

    @JsonProperty("credentialsId")
    private String credentialsId;

    @JsonDeserialize(converter = ProtectedValueMapInConverter.class)
    @JsonSerialize(converter = ProtectedValueMapOutConverter.class)
    @JsonProperty("values")
    private Map<String, ProtectedValue> values;

    public String getCredentialsId() {
        return credentialsId;
    }

    public Map<String, ProtectedValue> getValues() {
        return values;
    }
}
