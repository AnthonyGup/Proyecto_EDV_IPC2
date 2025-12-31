package com.backend.exceptions;

/**
 * Checked exception to signal business validation errors with an HTTP-like status code.
 */
public class ServiceException extends Exception {

    private final int statusCode;

    public ServiceException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
