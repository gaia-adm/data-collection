package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class AgentConfig {

    @JsonProperty("gaiaLocation")
    private String gaiaLocation;

    // valid only for on-prem deployment, for cloud deployment there is accessToken per tenant
    @JsonDeserialize(converter = ProtectedValueInConverter.class)
    @JsonSerialize(converter = ProtectedValueOutConverter.class)
    @JsonProperty("accessToken")
    private ProtectedValue accessToken;

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

    public ProtectedValue getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(final ProtectedValue accessToken) {
        this.accessToken = accessToken;
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

    public void setGaiaLocation(String gaiaLocation) {
        this.gaiaLocation = gaiaLocation;
    }

    public void setWorkerPool(Integer workerPool) {
        this.workerPool = workerPool;
    }

    public void setSoTimeout(Integer soTimeout) {
        this.soTimeout = soTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
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
