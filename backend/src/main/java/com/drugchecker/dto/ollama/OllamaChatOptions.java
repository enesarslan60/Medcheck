package com.drugchecker.dto.ollama;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generation options for /api/chat.
 * {@code num_predict} is Ollama's equivalent of max_tokens.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OllamaChatOptions(
        Double temperature,
        Integer num_predict
) {
}
