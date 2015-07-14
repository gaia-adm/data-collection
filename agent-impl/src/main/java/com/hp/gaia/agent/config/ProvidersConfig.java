package com.hp.gaia.agent.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ProvidersConfig {

    @JsonProperty("providers")
    private Map<String, ProviderConfig> providers;

    /**
     * Common proxy for data providers if not overridden.
     */
    @JsonProperty("proxy")
    private Proxy proxy;

    public Map<String, ProviderConfig> getProviders() {
        return providers;
    }

    public Proxy getProxy() {
        return proxy;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ProvidersConfig{");
        sb.append("providers=").append(providers);
        sb.append(", proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
