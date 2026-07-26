package com.drugchecker.dto.rxnorm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RxNormApproximateGroup(
        String inputTerm,
        List<RxNormUpstreamCandidate> candidate
) {
}
