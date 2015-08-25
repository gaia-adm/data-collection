package com.hp.gaia.provider.alm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.StringWriter;

public class JsonSerializer {

    private static ObjectMapper objectMapper;

    private JsonSerializer() {
    }

    public static <E> E deserialize(String jsonString, Class<E> clazz) {
        if (!StringUtils.isEmpty(jsonString)) {
            try {
                return getObjectMapper().readValue(jsonString, clazz);
            } catch (IOException e) {
                throw new RuntimeException("Failed to deserialize JSON string", e);
            }
        } else {
            return null;
        }
    }

    public static String serialize(Object object) {
        if (object != null) {
            StringWriter sw = new StringWriter();
            try {
                getObjectMapper().writeValue(sw, object);
                return sw.toString();
            } catch (IOException e) {
                throw new RuntimeException("Failed to serialize into JSON", e);
            }
        } else {
            return null;
        }
    }

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
        return objectMapper;
    }
}
