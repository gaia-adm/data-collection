package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Credentials {

    @JsonProperty("encrypted")
    private boolean encrypted;

    @JsonProperty("values")
    private Map<String, String> values;

    public boolean isEncrypted() {
        return encrypted;
    }

    public Map<String, String> getValues() {
        return values;
    }
}
