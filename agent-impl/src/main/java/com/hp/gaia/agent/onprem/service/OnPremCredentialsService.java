package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.Credentials;
import com.hp.gaia.agent.onprem.config.ConfigFactory;
import com.hp.gaia.agent.onprem.config.CredentialsConfig;
import com.hp.gaia.agent.service.CredentialsService;
import org.apache.commons.lang.Validate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class OnPremCredentialsService extends ConfigurationService implements CredentialsService {

    private static final String CREDENTIALS_CONFIG = "credentials.json";

    private Map<String, Credentials> credentialsMap;

    @PostConstruct
    public void init() {
        File credentialsConfigFile = getConfigFile(CREDENTIALS_CONFIG);
        verifyFile(credentialsConfigFile);

        CredentialsConfig credentialsConfig = ConfigFactory.readCredentialsConfig(credentialsConfigFile);
        credentialsMap = credentialsConfig.getCredentialsMap();
        if (credentialsMap == null) {
            credentialsMap = new HashMap<>();
        }
    }

    @Override
    public Map<String, String> getCredentials(final String credentialsId) {
        Validate.notNull(credentialsId);

        Credentials credentials = credentialsMap.get(credentialsId);
        if (credentials == null) {
            throw new IllegalArgumentException("Invalid credentialsId - " + credentialsId);
        }
        // TODO: implement decryption of encryptedValues for each getCredentials call
        Map<String, String> values = credentials.getValues();
        if (values == null) {
            return Collections.emptyMap();
        } else {
            return Collections.unmodifiableMap(values);
        }
    }
}
