package com.drugchecker.service;

import com.drugchecker.dto.openfda.OpenFdaLabelResponse;
import com.drugchecker.dto.openfda.OpenFdaLabelResult;
import com.drugchecker.exception.OpenFdaApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * openFDA drug/label lookup — fetches the "drug_interactions" (section 7) free text.
 * Primary search key is {@code openfda.rxcui}; falls back to {@code openfda.generic_name}.
 */
@Service
public class OpenFDAService {

    private static final Logger log = LoggerFactory.getLogger(OpenFDAService.class);

    private final WebClient openFdaWebClient;
    private final String apiKey;

    public OpenFDAService(@Qualifier("openFdaWebClient") WebClient openFdaWebClient,
                          @Value("${openfda.api-key:}") String apiKey) {
        this.openFdaWebClient = openFdaWebClient;
        this.apiKey = apiKey;
    }

    /**
     * Look up the drug_interactions text for the given identifier.
     * The identifier is tried as an RxCUI first, then as a generic name.
     *
     * @return the interaction text if any label was found, otherwise {@link Optional#empty()}
     */
    public Optional<String> getDrugInteractionText(String rxcuiOrGeneric) {
        if (rxcuiOrGeneric == null || rxcuiOrGeneric.isBlank()) {
            return Optional.empty();
        }
        String value = rxcuiOrGeneric.trim();

        Optional<String> byRxcui = queryLabel("openfda.rxcui:" + value);
        if (byRxcui.isPresent()) {
            return byRxcui;
        }
        return queryLabel("openfda.generic_name:\"" + value + "\"");
    }

    private Optional<String> queryLabel(String searchExpression) {
        OpenFdaLabelResponse response;
        try {
            response = openFdaWebClient.get()
                    .uri(buildUri(searchExpression))
                    .retrieve()
                    .bodyToMono(OpenFdaLabelResponse.class)
                    .block();
        } catch (WebClientResponseException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                // openFDA returns 404 when there is no match — treat as empty
                return Optional.empty();
            }
            log.warn("openFDA returned {} for search '{}'", ex.getStatusCode(), searchExpression);
            throw new OpenFdaApiException(
                    "openFDA request failed: " + ex.getStatusCode(), ex);
        } catch (RuntimeException ex) {
            throw new OpenFdaApiException("openFDA request failed", ex);
        }

        return extractInteractionText(response);
    }

    private Function<UriBuilder, URI> buildUri(String searchExpression) {
        return uri -> {
            UriBuilder b = uri.path("/drug/label.json")
                    .queryParam("search", searchExpression)
                    .queryParam("limit", 1);
            if (apiKey != null && !apiKey.isBlank()) {
                b = b.queryParam("api_key", apiKey);
            }
            return b.build();
        };
    }

    private Optional<String> extractInteractionText(OpenFdaLabelResponse response) {
        if (response == null || response.results() == null || response.results().isEmpty()) {
            return Optional.empty();
        }
        OpenFdaLabelResult first = response.results().get(0);
        List<String> interactions = first.drug_interactions();
        if (interactions == null || interactions.isEmpty()) {
            return Optional.empty();
        }
        String joined = String.join("\n\n", interactions).trim();
        return joined.isEmpty() ? Optional.empty() : Optional.of(joined);
    }
}
