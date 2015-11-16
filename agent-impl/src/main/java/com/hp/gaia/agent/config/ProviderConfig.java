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

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

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

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProviderConfig)) {
            return false;
        }

        final ProviderConfig that = (ProviderConfig) o;

        if (configId != null ? !configId.equals(that.configId) : that.configId != null) {
            return false;
        }
        if (providerId != null ? !providerId.equals(that.providerId) : that.providerId != null) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
            return false;
        }
        if (credentialsId != null ? !credentialsId.equals(that.credentialsId) : that.credentialsId != null) {
            return false;
        }
        if (proxy != null ? !proxy.equals(that.proxy) : that.proxy != null) {
            return false;
        }
        return !(runPeriod != null ? !runPeriod.equals(that.runPeriod) : that.runPeriod != null);

    }

    @Override
    public int hashCode() {
        int result = configId != null ? configId.hashCode() : 0;
        result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (credentialsId != null ? credentialsId.hashCode() : 0);
        result = 31 * result + (proxy != null ? proxy.hashCode() : 0);
        result = 31 * result + (runPeriod != null ? runPeriod.hashCode() : 0);
        return result;
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
