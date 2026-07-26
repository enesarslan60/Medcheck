package com.drugchecker.dto;

/**
 * Clean, external representation of a single RxNorm approximateTerm candidate.
 * This is what the API returns to the frontend / callers.
 */
public record RxNormCandidate(
        String rxcui,
        String name,
        int score,
        int rank
) {
}
