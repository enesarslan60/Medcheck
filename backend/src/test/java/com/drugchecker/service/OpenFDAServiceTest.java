package com.drugchecker.service;

import com.drugchecker.config.WebClientConfig;
import com.drugchecker.exception.OpenFdaApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenFDAServiceTest {

    private static final String INTERACTION_TEXT = "Do not combine with anticoagulants.";

    private MockWebServer server;
    private OpenFDAService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        var webClient = new WebClientConfig().openFdaWebClient(server.url("/").toString());
        service = new OpenFDAService(webClient, "");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void getDrugInteractionText_returnsTextOnRxcuiHit() throws InterruptedException {
        server.enqueue(labelResponse(INTERACTION_TEXT));

        Optional<String> text = service.getDrugInteractionText("5640");

        assertThat(text).contains(INTERACTION_TEXT);
        assertThat(server.getRequestCount()).isEqualTo(1);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath())
                .startsWith("/drug/label.json")
                .contains("search=openfda.rxcui:5640");
    }

    @Test
    void getDrugInteractionText_fallsBackToGenericNameWhenRxcuiReturns404()
            throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(404));
        server.enqueue(labelResponse(INTERACTION_TEXT));

        Optional<String> text = service.getDrugInteractionText("ibuprofen");

        assertThat(text).contains(INTERACTION_TEXT);
        assertThat(server.getRequestCount()).isEqualTo(2);

        RecordedRequest first = server.takeRequest();
        assertThat(first.getPath()).contains("search=openfda.rxcui:ibuprofen");

        RecordedRequest second = server.takeRequest();
        assertThat(second.getPath()).contains("openfda.generic_name");
    }

    @Test
    void getDrugInteractionText_returnsEmptyWhenBothQueriesReturn404() {
        server.enqueue(new MockResponse().setResponseCode(404));
        server.enqueue(new MockResponse().setResponseCode(404));

        assertThat(service.getDrugInteractionText("nonexistent")).isEmpty();
    }

    @Test
    void getDrugInteractionText_blankInputSkipsNetwork() {
        assertThat(service.getDrugInteractionText("  ")).isEmpty();
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void getDrugInteractionText_serverErrorRaisesOpenFdaApiException() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> service.getDrugInteractionText("5640"))
                .isInstanceOf(OpenFdaApiException.class)
                .hasMessageContaining("500");
    }

    @Test
    void getDrugInteractionText_missingInteractionsFieldFallsBackAndReturnsEmpty() {
        // primary hit but no drug_interactions -> fall back to generic_name -> still empty
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        { "meta": {}, "results": [ { "openfda": { "rxcui": ["5640"] } } ] }
                        """));
        server.enqueue(new MockResponse().setResponseCode(404));

        assertThat(service.getDrugInteractionText("5640")).isEmpty();
    }

    @Test
    void getDrugInteractionText_includesApiKeyWhenConfigured() throws InterruptedException {
        var webClient = new WebClientConfig().openFdaWebClient(server.url("/").toString());
        var withKey = new OpenFDAService(webClient, "TEST_KEY");
        server.enqueue(labelResponse(INTERACTION_TEXT));

        withKey.getDrugInteractionText("5640");

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).contains("api_key=TEST_KEY");
    }

    private MockResponse labelResponse(String interactionText) {
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "meta": {"disclaimer": "test"},
                          "results": [
                            {
                              "drug_interactions": ["%s"],
                              "openfda": {"rxcui": ["5640"], "generic_name": ["ibuprofen"]}
                            }
                          ]
                        }
                        """.formatted(interactionText));
    }
}
