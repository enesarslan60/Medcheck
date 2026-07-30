package com.drugchecker.dto.anthropic;

import com.drugchecker.model.Severity;

/**
 * Internal DTO — represents a parsed structured response from Claude for
 * an interaction analysis. {@code parsed=false} means the model returned
 * text that couldn't be interpreted as JSON with the expected fields.
 */
public record LlmVerdict(Severity severity, String summary, boolean parsed) {

    public static LlmVerdict fallback(String rawText) {
        return new LlmVerdict(Severity.UNKNOWN, rawText, false);
    }
}
