package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentConfig {

    @JsonProperty("gaiaLocation")
    private String gaiaLocation;

    // valid only for on-prem deployment, for cloud deployment there is accessToken per tenant
    @JsonProperty("accessToken")
    private String accessToken;

    /**
     * Proxy for connecting to GAIA.
     */
    @JsonProperty("proxy")
    private Proxy proxy;

    @JsonProperty("soTimeout")
    private int soTimeout;

    @JsonProperty("connectionTimeout")
    private int connectionTimeout;

    public String getGaiaLocation() {
        return gaiaLocation;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public int getSoTimeout() {
        return soTimeout;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentConfig{");
        sb.append("gaiaLocation='").append(gaiaLocation).append('\'');
        sb.append(", soTimeout=").append(soTimeout);
        sb.append(", connectionTimeout=").append(connectionTimeout);
        sb.append(", proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
