package com.drugchecker.service;

import com.drugchecker.dto.RxNormCandidate;
import com.drugchecker.dto.rxnorm.RxNormApproximateResponse;
import com.drugchecker.dto.rxnorm.RxNormUpstreamCandidate;
import com.drugchecker.exception.RxNormApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Fuzzy free-text drug lookup backed by RxNorm's approximateTerm endpoint.
 * Tolerates typos and non-canonical brand names.
 */
@Service
public class RxNormService {

    private static final Logger log = LoggerFactory.getLogger(RxNormService.class);
    private static final int DEFAULT_MAX_ENTRIES = 10;

    private final WebClient rxNormWebClient;

    public RxNormService(@Qualifier("rxNormWebClient") WebClient rxNormWebClient) {
        this.rxNormWebClient = rxNormWebClient;
    }

    public List<RxNormCandidate> searchDrugs(String query) {
        return searchDrugs(query, DEFAULT_MAX_ENTRIES);
    }

    public List<RxNormCandidate> searchDrugs(String query, int maxEntries) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        RxNormApproximateResponse response;
        try {
            response = rxNormWebClient.get()
                    .uri(uri -> uri.path("/approximateTerm.json")
                            .queryParam("term", query.trim())
                            .queryParam("maxEntries", maxEntries)
                            .build())
                    .retrieve()
                    .bodyToMono(RxNormApproximateResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("RxNorm returned {} for term '{}'", ex.getStatusCode(), query);
            throw new RxNormApiException(
                    "RxNorm request failed: " + ex.getStatusCode(), ex);
        } catch (RuntimeException ex) {
            throw new RxNormApiException("RxNorm request failed", ex);
        }

        if (response == null
                || response.approximateGroup() == null
                || response.approximateGroup().candidate() == null) {
            return List.of();
        }

        return response.approximateGroup().candidate().stream()
                .filter(Objects::nonNull)
                .filter(c -> c.rxcui() != null && !c.rxcui().isBlank())
                .map(this::toCandidate)
                .toList();
    }

    private RxNormCandidate toCandidate(RxNormUpstreamCandidate c) {
        return new RxNormCandidate(
                c.rxcui(),
                c.name(),
                parseIntSafe(c.score()),
                parseIntSafe(c.rank())
        );
    }

    private int parseIntSafe(String s) {
        if (s == null || s.isBlank()) return 0;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
