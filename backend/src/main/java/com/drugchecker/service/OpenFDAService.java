package com.drugchecker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenFDAService {

    private static final Logger log = LoggerFactory.getLogger(OpenFDAService.class);
    private static final String BASE_URL = "https://api.fda.gov/drug/label.json";

    private final RestTemplate restTemplate;

    public OpenFDAService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String searchInteractions(String substance1, String substance2) {
        if (substance1 == null || substance2 == null) {
            return null;
        }
        try {
            String query = "drug_interactions:" + substance1 + "+AND+" + substance2;
            URI uri = UriComponentsBuilder.fromUriString(BASE_URL)
                    .queryParam("search", query)
                    .queryParam("limit", 1)
                    .build()
                    .toUri();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) {
                return null;
            }
            Object results = response.get("results");
            if (results instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?, ?> entry) {
                    Object interactions = entry.get("drug_interactions");
                    if (interactions instanceof List<?> il && !il.isEmpty()) {
                        return il.get(0).toString();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("OpenFDA interaction lookup failed for {} + {}: {}",
                    substance1, substance2, e.getMessage());
            return null;
        }
    }

    public Map<String, Object> searchDrugInfo(String substanceName) {
        Map<String, Object> result = new HashMap<>();
        if (substanceName == null || substanceName.isBlank()) {
            return result;
        }
        try {
            String query = "openfda.substance_name:" + substanceName;
            URI uri = UriComponentsBuilder.fromUriString(BASE_URL)
                    .queryParam("search", query)
                    .queryParam("limit", 1)
                    .build()
                    .toUri();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) {
                return result;
            }
            Object results = response.get("results");
            if (results instanceof List<?> list && !list.isEmpty()
                    && list.get(0) instanceof Map<?, ?> entry) {

                result.put("brandName", firstOrNull(nestedList(entry, "openfda", "brand_name")));
                result.put("description", firstOrNull(asList(entry.get("description"))));
                result.put("warnings", firstOrNull(asList(entry.get("warnings"))));
                result.put("adverseReactions", firstOrNull(asList(entry.get("adverse_reactions"))));
            }
            return result;
        } catch (Exception e) {
            log.warn("OpenFDA drug info lookup failed for {}: {}", substanceName, e.getMessage());
            return result;
        }
    }

    private List<?> asList(Object o) {
        return o instanceof List<?> l ? l : List.of();
    }

    private List<?> nestedList(Map<?, ?> entry, String outer, String inner) {
        Object o = entry.get(outer);
        if (o instanceof Map<?, ?> m) {
            return asList(m.get(inner));
        }
        return List.of();
    }

    private String firstOrNull(List<?> list) {
        return list.isEmpty() ? null : String.valueOf(list.get(0));
    }
}
