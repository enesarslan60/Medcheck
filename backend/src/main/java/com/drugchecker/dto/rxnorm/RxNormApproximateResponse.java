package com.drugchecker.dto.rxnorm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Root of the upstream response from
 * https://rxnav.nlm.nih.gov/REST/approximateTerm.json
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RxNormApproximateResponse(RxNormApproximateGroup approximateGroup) {
}
