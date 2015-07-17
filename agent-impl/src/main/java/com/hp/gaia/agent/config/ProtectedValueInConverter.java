package com.hp.gaia.agent.config;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.hp.gaia.agent.config.ProtectedValue.Type;

import java.util.Map;

/**
 * Jackson converter for deserialization of {@link ProtectedValue}.
 */
public class ProtectedValueInConverter extends StdConverter<Map<String, Object>, ProtectedValue> {

    @Override
    public ProtectedValue convert(final Map<String, Object> inValue) {
        if (inValue != null && !inValue.isEmpty()) {
            if (inValue.size() > 1) {
                throw new IllegalStateException("Expected only 1 key, got keys [" + inValue.keySet().toString() + "]");
            }
            String typeId = inValue.keySet().iterator().next();
            Type type = Type.fromId(typeId);
            return new ProtectedValue(type, (String) inValue.get(typeId));
        }
        return null;
    }
}
