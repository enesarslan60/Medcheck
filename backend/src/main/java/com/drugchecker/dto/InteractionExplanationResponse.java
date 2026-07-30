package com.drugchecker.dto;

import com.drugchecker.model.Severity;

import java.util.List;

/**
 * Top-level response for POST /api/interactions/check.
 *
 * @param severity           LLM-classified risk level ({@link Severity#UNKNOWN} on LLM failure)
 * @param interactionSummary short German explanation, {@code null} on LLM failure
 * @param drugs              per-drug details (raw FDA text always present when available,
 *                           AI summary only when {@code llmAvailable == true})
 * @param llmAvailable       {@code false} indicates a graceful degradation — the frontend
 *                           should render a neutral badge and a "AI unavailable" hint
 */
public record InteractionExplanationResponse(
        Severity severity,
        String interactionSummary,
        List<DrugDetail> drugs,
        boolean llmAvailable
) {
}
