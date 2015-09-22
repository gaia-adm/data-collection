package com.hp.gaia.provider.circleci.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class JsonUtils {

    private JsonUtils() {
    }

    public static String getStringValue(ObjectNode objectNode, String fieldName) {
        JsonNode jsonNode = objectNode.get(fieldName);
        if (jsonNode instanceof TextNode) {
            return jsonNode.asText();
        } else if (jsonNode instanceof NullNode || jsonNode == null) {
            return null;
        } else {
            throw new IllegalStateException("Unexpected value type for field '" + fieldName + "'");
        }
    }
}
