package com.drugchecker.service;

import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.exception.GeminiApiException;
import com.drugchecker.model.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiServiceTest {

    private static final String MODEL = "gemini-test-model";

    private MockWebServer server;
    private GeminiService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service = new GeminiService(buildClient(5), new ObjectMapper(), MODEL, 256, "test-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void explainInteraction_parsesStructuredVerdict() throws InterruptedException {
        server.enqueue(geminiResponse("""
                {
                  "severity": "HIGH",
                  "summary": "Die gleichzeitige Einnahme kann Blutungen verstärken."
                }
                """));

        LlmVerdict verdict = service.explainInteraction(
                List.of("warfarin", "ibuprofen"),
                List.of("warfarin text", "ibuprofen text"));

        assertThat(verdict.severity()).isEqualTo(Severity.HIGH);
        assertThat(verdict.summary()).contains("Blutungen");
        assertThat(verdict.parsed()).isTrue();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath())
                .startsWith("/v1beta/models/" + MODEL + ":generateContent")
                .contains("key=test-key");
        String body = req.getBody().readUtf8();
        assertThat(body).contains("systemInstruction").contains("warfarin");
    }

    @Test
    void explainInteraction_invalidJsonFallsBackToUnknown() {
        server.enqueue(geminiResponse("Sorry, cannot help with medical advice."));

        LlmVerdict verdict = service.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb"));

        assertThat(verdict.severity()).isEqualTo(Severity.UNKNOWN);
        assertThat(verdict.parsed()).isFalse();
    }

    @Test
    void explainInteraction_http500RaisesGeminiApiException() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"error\":\"boom\"}"));

        assertThatThrownBy(() -> service.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb")))
                .isInstanceOf(GeminiApiException.class)
                .hasMessageContaining("500");
    }

    @Test
    void summarizeSideEffects_returnsPlainText() {
        server.enqueue(geminiResponse("Kann zu Magenblutungen führen."));

        String summary = service.summarizeSideEffects("ibuprofen", "GI bleeding risk...");

        assertThat(summary).startsWith("Kann zu Magenblutungen");
    }

    @Test
    void callWithoutApiKeyIsRejected() {
        GeminiService unconfigured = new GeminiService(
                buildClient(5), new ObjectMapper(), MODEL, 256, "");

        assertThat(unconfigured.isConfigured()).isFalse();
        assertThatThrownBy(() -> unconfigured.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb")))
                .isInstanceOf(GeminiApiException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void requestTimesOutOnSlowServer() {
        GeminiService fast = new GeminiService(
                buildClient(1), new ObjectMapper(), MODEL, 256, "test-key");

        server.enqueue(geminiResponse("{\"severity\":\"LOW\",\"summary\":\"...\"}")
                .setBodyDelay(3, TimeUnit.SECONDS));

        assertThatThrownBy(() -> fast.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb")))
                .isInstanceOf(GeminiApiException.class);
    }

    private WebClient buildClient(int timeoutSeconds) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutSeconds * 1000)
                .responseTimeout(Duration.ofSeconds(timeoutSeconds));
        return WebClient.builder()
                .baseUrl(server.url("/").toString())
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .build();
    }

    private MockResponse geminiResponse(String text) {
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "candidates": [{
                            "content": {
                              "role": "model",
                              "parts": [{"text": "%s"}]
                            },
                            "finishReason": "STOP"
                          }]
                        }
                        """.formatted(escaped));
    }
}
