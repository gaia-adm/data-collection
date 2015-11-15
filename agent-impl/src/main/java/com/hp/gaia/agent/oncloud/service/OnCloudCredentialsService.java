package com.hp.gaia.agent.oncloud.service;

import com.hp.gaia.agent.config.Credentials;
import com.hp.gaia.agent.config.ProtectedValue;
import com.hp.gaia.agent.config.ProtectedValue.Type;
import com.hp.gaia.agent.onprem.config.CredentialsConfig;
import com.hp.gaia.agent.service.ProtectedValueDecrypter;
import com.hp.gaia.agent.service.UpdatableCredentialsService;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnCloudCredentialsService implements UpdatableCredentialsService {

    private Map<String, Credentials> credentialsMap;

    @Autowired
    private ProtectedValueDecrypter protectedValueDecrypter;

    public void init() {

        credentialsMap = new HashMap<>();
        System.out.println("OnCloudCredentialsService initialized");
    }

    @Override
    public void addCredentials(@NotNull String key, @NotNull Credentials credentials) {

        if (key != null && credentials != null) {
            credentialsMap.put(key, credentials);
        } else {
            throw new IllegalArgumentException("Null is not allowed for key/value: " + key + ", " + credentials);
        }


        System.out.println("credentials added for " + key);

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
