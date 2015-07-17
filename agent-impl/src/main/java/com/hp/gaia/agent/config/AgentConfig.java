package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentConfig {

    @JsonProperty("gaiaLocation")
    private String gaiaLocation;

    // valid only for on-prem deployment, for cloud deployment there is accessToken per tenant
    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("workerPool")
    private Integer workerPool;

    /**
     * Proxy for connecting to GAIA.
     */
    @JsonProperty("proxy")
    private Proxy proxy;

    @JsonProperty("soTimeout")
    private Integer soTimeout;

    @JsonProperty("connectTimeout")
    private Integer connectTimeout;

    public String getGaiaLocation() {
        return gaiaLocation;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Integer getWorkerPool() {
        return workerPool;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Integer getSoTimeout() {
        return soTimeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AgentConfig{");
        sb.append("gaiaLocation='").append(gaiaLocation).append('\'');
        sb.append(", workerPool=").append(workerPool);
        sb.append(", soTimeout=").append(soTimeout);
        sb.append(", connectTimeout=").append(connectTimeout);
        sb.append(", proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
