package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ProviderConfig {

    @JsonProperty("configId")
    private String configId;

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

    /**
     * Run period in minutes.
     */
    @JsonProperty("runPeriod")
    private Integer runPeriod;

    public ProviderConfig() {
    }

    public ProviderConfig(final String configId, final String providerId, final Map<String, String> properties,
                          final String credentialsId, final Proxy proxy, final Integer runPeriod) {
        this.configId = configId;
        this.providerId = providerId;
        this.properties = properties;
        this.credentialsId = credentialsId;
        this.proxy = proxy;
        this.runPeriod = runPeriod;
    }

    public String getConfigId() {
        return configId;
    }

    public String getProviderId() {
        return providerId;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Integer getRunPeriod() {
        return runPeriod;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProviderConfig{");
        sb.append("configId='").append(configId).append('\'');
        sb.append(", providerId='").append(providerId).append('\'');
        sb.append(", properties=").append(properties);
        sb.append(", credentialsId='").append(credentialsId).append('\'');
        sb.append(", proxy=").append(proxy);
        sb.append(", runPeriod=").append(runPeriod);
        sb.append('}');
        return sb.toString();
    }
}
