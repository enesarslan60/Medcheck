package com.drugchecker.dto.openfda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenFdaLabelOpenFda(
        List<String> brand_name,
        List<String> generic_name,
        List<String> substance_name,
        List<String> rxcui
) {
}
