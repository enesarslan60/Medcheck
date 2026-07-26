package com.drugchecker.dto.openfda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * One label entry. Interaction text (section 7) lives in {@code drug_interactions}.
 * openFDA returns it as an array of strings — usually one long paragraph.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenFdaLabelResult(
        List<String> drug_interactions,
        OpenFdaLabelOpenFda openfda
) {
}
