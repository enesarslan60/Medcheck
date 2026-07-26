package com.drugchecker.service;

import com.drugchecker.config.WebClientConfig;
import com.drugchecker.dto.RxNormCandidate;
import com.drugchecker.exception.RxNormApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RxNormServiceTest {

    private MockWebServer server;
    private RxNormService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        var webClient = new WebClientConfig().rxNormWebClient(server.url("/").toString());
        service = new RxNormService(webClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void searchDrugs_parsesCandidates() throws InterruptedException {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "approximateGroup": {
                            "inputTerm": "ibprofen",
                            "candidate": [
                              { "rxcui": "5640", "score": "100", "rank": "1",
                                "name": "ibuprofen", "source": "RXNORM" },
                              { "rxcui": "153010", "score": "75", "rank": "2",
                                "name": "ibuprofen lysine", "source": "RXNORM" }
                            ]
                          }
                        }
                        """));

        List<RxNormCandidate> results = service.searchDrugs("ibprofen");

        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isEqualTo(new RxNormCandidate("5640", "ibuprofen", 100, 1));
        assertThat(results.get(1).rxcui()).isEqualTo("153010");
        assertThat(results.get(1).score()).isEqualTo(75);

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath())
                .startsWith("/approximateTerm.json")
                .contains("term=ibprofen")
                .contains("maxEntries=10");
    }

    @Test
    void searchDrugs_emptyCandidatesReturnsEmptyList() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        { "approximateGroup": { "inputTerm": "asdfghjkl" } }
                        """));

        assertThat(service.searchDrugs("asdfghjkl")).isEmpty();
    }

    @Test
    void searchDrugs_blankQueryDoesNotHitTheNetwork() {
        assertThat(service.searchDrugs("   ")).isEmpty();
        assertThat(server.getRequestCount()).isZero();
    }

    @Test
    void searchDrugs_serverErrorRaisesRxNormApiException() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> service.searchDrugs("ibuprofen"))
                .isInstanceOf(RxNormApiException.class)
                .hasMessageContaining("500");
    }
}
