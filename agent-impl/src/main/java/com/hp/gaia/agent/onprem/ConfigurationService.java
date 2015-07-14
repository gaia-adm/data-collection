package com.hp.gaia.agent.onprem;

import com.hp.gaia.agent.config.AgentConfig;
import com.hp.gaia.agent.config.CredentialsConfig;
import com.hp.gaia.agent.config.ProvidersConfig;

public class ConfigurationService {

    private AgentConfig agentConfig;

    private ProvidersConfig providersConfig;

    private CredentialsConfig credentialsConfig;

    public void init(AgentConfig agentConfig, ProvidersConfig providersConfig, CredentialsConfig credentialsConfig) {
        this.agentConfig = agentConfig;
        this.providersConfig = providersConfig;
        this.credentialsConfig = credentialsConfig;
    }

    public AgentConfig getAgentConfig() {
        return agentConfig;
    }

    public ProvidersConfig getProvidersConfig() {
        return providersConfig;
    }

    public CredentialsConfig getCredentialsConfig() {
        return credentialsConfig;
    }
}
