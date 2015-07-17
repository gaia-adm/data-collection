package com.hp.gaia.agent.config;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Jackson converter for deserialization of map of {@link ProtectedValue} values.
 */
public class ProtectedValueMapInConverter extends StdConverter<Map<String, Object>, Map<String, ProtectedValue>> {

    private static final ProtectedValueInConverter childConverter = new ProtectedValueInConverter();

    @Override
    public Map<String, ProtectedValue> convert(final Map<String, Object> credentialsMap) {
        if (credentialsMap == null) {
            return null;
        }
        Map<String, ProtectedValue> protectedValueMap = new HashMap<>();
        for (Map.Entry<String, Object> credential : credentialsMap.entrySet()) {
            String credentialKey = credential.getKey();
            ProtectedValue value = null;
            if (credential.getValue() instanceof Map) {
                value = childConverter.convert((Map<String, Object>) credential.getValue());
            }
            protectedValueMap.put(credentialKey, value);

        }
        return protectedValueMap;
    }
}
