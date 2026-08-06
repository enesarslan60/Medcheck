package com.drugchecker.dto.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** Request body for POST http://localhost:11434/api/chat */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OllamaChatRequest(
        String model,
        boolean stream,
        List<OllamaChatMessage> messages,
        OllamaChatOptions options
) {
}
