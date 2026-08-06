package com.drugchecker.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiCandidate(GeminiContent content, String finishReason) {
}
