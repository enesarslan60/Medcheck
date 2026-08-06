package com.drugchecker.exception;

/**
 * Thrown for non-recoverable failures against a local Ollama instance —
 * typically because Ollama isn't running on the configured base URL or
 * the requested model hasn't been pulled yet.
 * Handled the same way as {@link AnthropicApiException} and
 * {@link GeminiApiException}: caught in InteractionService and turned
 * into a graceful fallback response.
 */
public class OllamaApiException extends RuntimeException {

    public OllamaApiException(String message) {
        super(message);
    }

    public OllamaApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
