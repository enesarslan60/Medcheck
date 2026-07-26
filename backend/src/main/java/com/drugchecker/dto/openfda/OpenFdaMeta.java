package com.drugchecker.dto.openfda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenFdaMeta(
        String disclaimer,
        String terms,
        String license,
        String last_updated
) {
}
