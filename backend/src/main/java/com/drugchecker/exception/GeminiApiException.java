package com.drugchecker.exception;

/**
 * Thrown for non-recoverable failures against the Google Gemini API.
 * Handled the same way as {@link AnthropicApiException} — caught in
 * {@link com.drugchecker.service.InteractionService} and turned into
 * a graceful fallback response.
 */
public class GeminiApiException extends RuntimeException {

    public GeminiApiException(String message) {
        super(message);
    }

    public GeminiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
