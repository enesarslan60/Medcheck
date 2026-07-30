package com.drugchecker.model;

/**
 * Risk classification of a drug-drug interaction as inferred by the LLM
 * from openFDA drug_interactions text.
 *
 * <p>Ordering is intentional: {@code LOW < MEDIUM < HIGH < UNKNOWN}.
 * {@code UNKNOWN} is used when the LLM did not return a valid classification
 * (e.g. because the LLM call failed or returned malformed JSON).
 */
public enum Severity {
    LOW,
    MEDIUM,
    HIGH,
    UNKNOWN;

    public static Severity fromString(String value) {
        if (value == null) return UNKNOWN;
        String normalized = value.trim().toUpperCase();
        for (Severity s : values()) {
            if (s.name().equals(normalized)) return s;
        }
        // Accept a few common synonyms the model may produce
        return switch (normalized) {
            case "MODERATE" -> MEDIUM;
            case "SEVERE", "CRITICAL" -> HIGH;
            case "MILD", "MINOR" -> LOW;
            default -> UNKNOWN;
        };
    }
}
