package com.hp.gaia.agent.config;

import java.util.Map;

public class CredentialsConfig {

    /**
     * Map of credentialsId to {@link Credentials}.
     */
    private final Map<String, Credentials> credentialsMap;

    public CredentialsConfig(final Map<String, Credentials> credentialsMap) {
        this.credentialsMap = credentialsMap;
    }

    public Map<String, Credentials> getCredentialsMap() {
        return credentialsMap;
    }
}
