package com.hp.gaia.agent.onprem.config;

import com.hp.gaia.agent.config.Credentials;

import java.util.List;

public class CredentialsConfig {

    /**
     * Map of credentialsId to {@link Credentials}.
     */
    private final List<Credentials> credentials;

    public CredentialsConfig(final List<Credentials> credentials) {
        this.credentials = credentials;
    }

    public List<Credentials> getCredentials() {
        return credentials;
    }
}
