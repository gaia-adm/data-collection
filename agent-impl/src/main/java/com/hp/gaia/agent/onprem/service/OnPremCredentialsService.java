package com.hp.gaia.agent.onprem.service;

import com.hp.gaia.agent.config.Credentials;
import com.hp.gaia.agent.config.ProtectedValue;
import com.hp.gaia.agent.config.ProtectedValue.Type;
import com.hp.gaia.agent.onprem.config.ConfigUtils;
import com.hp.gaia.agent.onprem.config.CredentialsConfig;
import com.hp.gaia.agent.service.CredentialsService;
import com.hp.gaia.agent.service.ProtectedValueDecrypter;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnPremCredentialsService implements CredentialsService {

    private Map<String, Credentials> credentialsMap;

    @Autowired
    private ProtectedValueDecrypter protectedValueDecrypter;

    public void init(File credentialsConfigFile) {
        CredentialsConfig credentialsConfig = ConfigUtils.readCredentialsConfig(credentialsConfigFile);
        boolean saveNewFile = encryptNeededValues(credentialsConfig);
        if (saveNewFile) {
            File newConfigFile = new File(credentialsConfigFile.getAbsolutePath() + ".encrypted");
            if (!newConfigFile.exists() || newConfigFile.canWrite()) {
                ConfigUtils.writeCredentialsConfig(newConfigFile, credentialsConfig);
            }
        }

        credentialsMap = new HashMap<>();
        List<Credentials> credentialsList = credentialsConfig.getCredentials();
        if (credentialsList != null) {
            for (Credentials credentials : credentialsList) {
                credentialsMap.put(credentials.getCredentialsId(), credentials);
            }
        }
    }

    @Override
    public Map<String, String> getCredentials(final String credentialsId) {
        Validate.notNull(credentialsId);

        Credentials credentials = credentialsMap.get(credentialsId);
        if (credentials == null) {
            throw new IllegalArgumentException("Invalid credentialsId - " + credentialsId);
        }
        Map<String, ProtectedValue> protectedValues = credentials.getValues();
        return Collections.unmodifiableMap(decryptValues(protectedValues));
    }

    /**
     * Checks if there are values that should be encrypted and encrypts them. Returns true if at least there was
     * one such value.
     */
    private boolean encryptNeededValues(CredentialsConfig credentialsConfig) {
        boolean result = false;
        List<Credentials> credentialsList = credentialsConfig.getCredentials();
        if (credentialsList != null) {
            for (Credentials credentials : credentialsList) {
                Map<String, ProtectedValue> protectedValueMap = credentials.getValues();
                if (protectedValueMap != null) {
                    for (Map.Entry<String, ProtectedValue> entry : protectedValueMap.entrySet()) {
                        if (entry.getValue().getType() == Type.ENCRYPT) {
                            ProtectedValue newProtectedValue = protectedValueDecrypter.encrypt(entry.getValue().getValue());
                            protectedValueMap.put(entry.getKey(), newProtectedValue);
                            result = true;
                        }
                    }
                }
            }
        }
        return result;
    }

    private Map<String, String> decryptValues(final Map<String, ProtectedValue> protectedValues) {
        Map<String, String> decryptedValues = new HashMap<>();
        if (protectedValues != null) {
            for (Map.Entry<String, ProtectedValue> protectedValueEntry : protectedValues.entrySet()) {
                ProtectedValue protectedValue = protectedValueEntry.getValue();
                if (protectedValue != null) {
                    if (protectedValue.getType() == Type.ENCRYPTED) {
                        decryptedValues.put(protectedValueEntry.getKey(), protectedValueDecrypter.decrypt(protectedValue));
                    } else {
                        decryptedValues.put(protectedValueEntry.getKey(), protectedValueEntry.getValue().getValue());
                    }
                } else {
                    decryptedValues.put(protectedValueEntry.getKey(), null);
                }
            }
        }
        return decryptedValues;
    }
}
