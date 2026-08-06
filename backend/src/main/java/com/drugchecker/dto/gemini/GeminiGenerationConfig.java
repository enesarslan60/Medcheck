package com.drugchecker.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiGenerationConfig(
        Integer maxOutputTokens,
        Double temperature
) {
}
