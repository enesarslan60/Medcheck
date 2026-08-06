package com.drugchecker.dto.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Request body for POST {model}:generateContent */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record GeminiRequest(
        List<GeminiContent> contents,
        @JsonProperty("systemInstruction") GeminiSystemInstruction systemInstruction,
        @JsonProperty("generationConfig") GeminiGenerationConfig generationConfig
) {
}
