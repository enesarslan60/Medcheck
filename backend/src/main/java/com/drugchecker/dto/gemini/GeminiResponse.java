package com.drugchecker.dto.gemini;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Response body from Gemini's generateContent endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeminiResponse(List<GeminiCandidate> candidates) {

    /** Concatenates all text parts from the first candidate. */
    public String firstText() {
        if (candidates == null || candidates.isEmpty()) return "";
        GeminiCandidate first = candidates.get(0);
        if (first == null || first.content() == null || first.content().parts() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (GeminiPart part : first.content().parts()) {
            if (part != null && part.text() != null) sb.append(part.text());
        }
        return sb.toString();
    }
}
