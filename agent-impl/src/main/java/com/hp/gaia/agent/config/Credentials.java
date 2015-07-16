package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Credentials {

    @JsonProperty("credentialsId")
    private String credentialsId;

    /**
     * If true then all credential values are encrypted.
     */
    @JsonProperty("encrypted")
    private boolean encrypted;

    @JsonProperty("values")
    private Map<String, String> values;

    public String getCredentialsId() {
        return credentialsId;
    }

    public boolean isEncrypted() {
        return encrypted;
    }

    public Map<String, String> getValues() {
        return values;
    }
}
