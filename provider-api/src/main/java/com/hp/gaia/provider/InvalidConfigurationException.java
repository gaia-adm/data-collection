package com.hp.gaia.provider;

/**
 * To be thrown when data provider receives configuration that is not valid and operation must not be retried. This could
 * be when a required parameter is missing or invalid. Normally it will be thrown even before the HTTP call is made.
 */
public class InvalidConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidConfigurationException(final String message) {
        super(message);
    }

    public InvalidConfigurationException(final String message, final Throwable t) {
        super(message, t);
    }
}
