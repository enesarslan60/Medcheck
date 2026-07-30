package com.drugchecker.exception;

/**
 * Thrown for non-recoverable failures against the Anthropic Messages API.
 * The InteractionService catches this and falls back to a raw-only response
 * — callers of the /check endpoint therefore never see this exception directly.
 */
public class AnthropicApiException extends RuntimeException {

    public AnthropicApiException(String message) {
        super(message);
    }

    public AnthropicApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
