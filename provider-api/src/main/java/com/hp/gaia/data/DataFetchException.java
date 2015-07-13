package com.hp.gaia.data;

/**
 * To be thrown when there was a general error when fetching data - network related, server returning an unexpected
 * status code. The operation may be retried later.
 */
public class DataFetchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private Integer statusCode;

    public DataFetchException(final String message) {
        super(message);
    }

    public DataFetchException(final String message, final int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public DataFetchException(final String message, final Throwable t) {
        super(message, t);
    }
}
