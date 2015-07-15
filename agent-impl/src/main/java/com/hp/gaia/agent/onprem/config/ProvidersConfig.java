package com.hp.gaia.agent.onprem.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hp.gaia.agent.config.ProviderConfig;
import com.hp.gaia.agent.config.Proxy;

import java.util.List;
import java.util.Map;

public class ProvidersConfig {

    @JsonProperty("providers")
    private List<ProviderConfig> providers;

    /**
     * Common proxy for data providers if not overridden.
     */
    @JsonProperty("proxy")
    private Proxy proxy;

    public List<ProviderConfig> getProviders() {
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
