package com.drugchecker.dto;

/**
 * Per-drug detail inside {@link InteractionExplanationResponse}.
 *
 * @param drugName            canonical name resolved via RxNorm
 * @param rxcui               RxNorm concept id, may be null
 * @param aiSideEffectSummary short German summary from the LLM, {@code null} on fallback
 * @param openFdaRawText      raw drug_interactions text from openFDA,
 *                            {@code null} if openFDA had nothing
 * @param source              where the raw text originated
 */
public record DrugDetail(
        String drugName,
        String rxcui,
        String aiSideEffectSummary,
        String openFdaRawText,
        String source
) {
}
