package com.drugchecker.dto.ollama;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response body from /api/chat (with {@code stream=false}). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaChatResponse(
        String model,
        OllamaChatMessage message,
        Boolean done
) {
    public String text() {
        return message == null || message.content() == null ? "" : message.content();
    }
}
