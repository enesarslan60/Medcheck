package com.drugchecker.dto.openfda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** Root of /drug/label.json */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenFdaLabelResponse(
        OpenFdaMeta meta,
        List<OpenFdaLabelResult> results
) {
}
