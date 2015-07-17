package com.hp.gaia.agent.config;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.util.HashMap;
import java.util.Map;

/**
 * Jackson converter for serialization of {@link ProtectedValue}.
 */
public class ProtectedValueOutConverter extends StdConverter<ProtectedValue, Map<String, Object>> {

    @Override
    public Map<String, Object> convert(final ProtectedValue protectedValue) {
        if (protectedValue != null) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(protectedValue.getType().getId(), protectedValue.getValue());
            return resultMap;
        }
        return null;
    }
}
