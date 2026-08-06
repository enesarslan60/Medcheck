package com.drugchecker.service;

import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.model.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provider-agnostic parsing of the structured {"severity": ..., "summary": ...}
 * JSON that the interaction prompt asks the model to return.
 * Used by both {@link AnthropicService} and {@link GeminiService}.
 */
final class LlmParsing {

    private static final Logger log = LoggerFactory.getLogger(LlmParsing.class);

    /** Matches the first {"..."} block — helps when the model wraps JSON in prose. */
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[\\s\\S]*\\}");

    private LlmParsing() {
    }

    /**
     * Parse the JSON verdict the model was asked to return. If parsing
     * fails, fall back to {@link Severity#UNKNOWN} and pass the raw text
     * through so the caller can decide how to present it.
     */
    static LlmVerdict parseVerdict(String rawResponse, ObjectMapper mapper) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return LlmVerdict.fallback("");
        }
        String candidate = extractJsonObject(rawResponse);
        if (candidate == null) {
            log.debug("Model response did not contain a JSON object; falling back. Body: {}", rawResponse);
            return LlmVerdict.fallback(rawResponse.trim());
        }
        try {
            JsonNode node = mapper.readTree(candidate);
            String severityText = node.hasNonNull("severity") ? node.get("severity").asText() : null;
            String summary = node.hasNonNull("summary") ? node.get("summary").asText() : null;
            Severity severity = Severity.fromString(severityText);
            if (summary == null || summary.isBlank()) {
                return LlmVerdict.fallback(rawResponse.trim());
            }
            return new LlmVerdict(severity, summary.trim(), true);
        } catch (Exception ex) {
            log.debug("Failed to parse LLM JSON verdict, using fallback. Cause: {}", ex.getMessage());
            return LlmVerdict.fallback(rawResponse.trim());
        }
    }

    private static String extractJsonObject(String text) {
        Matcher m = JSON_OBJECT_PATTERN.matcher(text);
        return m.find() ? m.group() : null;
    }
}
