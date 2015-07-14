package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ProviderConfig {

    @JsonProperty("providerId")
    private String providerId;

    @JsonProperty("properties")
    private Map<String, String> properties;

    @JsonProperty("credentialsId")
    private String credentialsId;

    /**
     * Provider specific proxy.
     */
    @JsonProperty("proxy")
    private Proxy proxy;

    @JsonProperty("scheduling")
    private SchedulingConfig schedulingConfig;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProviderConfig{");
        sb.append("providerId='").append(providerId).append('\'');
        sb.append(", properties=").append(properties);
        sb.append(", credentialsId='").append(credentialsId).append('\'');
        sb.append(", proxy=").append(proxy);
        sb.append(", schedulingConfig=").append(schedulingConfig);
        sb.append('}');
        return sb.toString();
    }
}
