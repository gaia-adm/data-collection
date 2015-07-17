package com.hp.gaia.agent.config;

/**
 * Represents a value that may be encrypted.
 */
public class ProtectedValue {

    public enum Type {
        /**
         * Value is plain - not secret.
         */
        PLAIN("plain"),
        /**
         * Value is secret and should be encrypted, but it is plain.
         */
        ENCRYPT("encrypt"),
        /**
         * Value is encrypted.
         */
        ENCRYPTED("encrypted");

        private String id;

        Type(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Type fromId(String id) {
            for (Type type : Type.values()) {
                if (type.id.equals(id)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("Unexpected type '" + id + "'");
        }
    }

    private final Type type;

    private final String value;

    public ProtectedValue(final Type type, final String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
