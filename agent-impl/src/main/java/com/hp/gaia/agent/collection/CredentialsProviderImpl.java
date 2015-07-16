package com.hp.gaia.agent.collection;

import com.hp.gaia.agent.service.CredentialsService;
import com.hp.gaia.provider.CredentialsProvider;

import java.util.Collections;
import java.util.Map;

public class CredentialsProviderImpl implements CredentialsProvider {

    private final CredentialsService credentialsService;

    private final String credentialsId;

    public CredentialsProviderImpl(final CredentialsService credentialsService, final String credentialsId) {
        this.credentialsService = credentialsService;
        this.credentialsId = credentialsId;
    }

    @Override
    public Map<String, String> getCredentials() {
        if (credentialsId != null) {
            return credentialsService.getCredentials(credentialsId);
        } else {
            return Collections.emptyMap();
        }
    }
}
