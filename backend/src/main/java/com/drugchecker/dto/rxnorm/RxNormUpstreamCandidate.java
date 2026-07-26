package com.drugchecker.dto.rxnorm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Upstream JSON shape — score and rank arrive as strings.
 * Kept internal to the rxnorm package; the service maps this to
 * {@link com.drugchecker.dto.RxNormCandidate} before returning.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RxNormUpstreamCandidate(
        String rxcui,
        String score,
        String rank,
        String name,
        String source
) {
}
