package com.drugchecker.exception;

/**
 * Thrown for any non-recoverable failure from the openFDA API.
 * A 404 (no results) is NOT an exception; the service returns {@code Optional.empty()} instead.
 */
public class OpenFdaApiException extends RuntimeException {

    public OpenFdaApiException(String message) {
        super(message);
    }

    public OpenFdaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
