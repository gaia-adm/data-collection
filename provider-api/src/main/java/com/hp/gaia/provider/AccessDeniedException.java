package com.hp.gaia.provider;

/**
 * To be thrown when provider cannot fetch data due to access being denied - due to invalid credentials, no credentials
 * etc. Operation in such case must not be retried.
 */
public class AccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AccessDeniedException(final String message) {
        super(message);
    }

    public AccessDeniedException(final String message, final Throwable t) {
        super(message, t);
    }
}
