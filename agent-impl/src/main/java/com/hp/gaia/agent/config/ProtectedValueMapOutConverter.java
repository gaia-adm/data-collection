package com.hp.gaia.agent.config;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Jackson converter for serialization of map of {@link ProtectedValue} s.
 */
public class ProtectedValueMapOutConverter extends StdConverter<Map<String, ProtectedValue>, Map<String, Object>> {

    private static final ProtectedValueOutConverter childConverter = new ProtectedValueOutConverter();

    @Override
    public Map<String, Object> convert(final Map<String, ProtectedValue> credentialsMap) {
        if (credentialsMap == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        for (Map.Entry<String, ProtectedValue> credentialEntry : credentialsMap.entrySet()) {
            Map<String, Object> value = childConverter.convert(credentialEntry.getValue());
            resultMap.put(credentialEntry.getKey(), value);
        }
        return resultMap;
    }
}
