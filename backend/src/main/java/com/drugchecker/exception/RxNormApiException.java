package com.drugchecker.exception;

/**
 * Thrown for any non-recoverable failure from the RxNorm API.
 * A missing-result response is NOT an exception; the service returns an empty list instead.
 */
public class RxNormApiException extends RuntimeException {

    public RxNormApiException(String message) {
        super(message);
    }

    public RxNormApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
