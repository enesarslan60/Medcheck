package com.drugchecker.service;

import com.drugchecker.config.WebClientConfig;
import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.exception.AnthropicApiException;
import com.drugchecker.model.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnthropicServiceTest {

    private static final String MODEL = "claude-test-model";

    private MockWebServer server;
    private AnthropicService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        var webClient = new WebClientConfig()
                .anthropicWebClient(server.url("/").toString(), "test-key", 5);
        service = new AnthropicService(webClient, new ObjectMapper(), MODEL, 256, "test-key");
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void explainInteraction_parsesStructuredVerdict() throws InterruptedException {
        server.enqueue(claudeResponse("""
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
        assertThat(req.getPath()).isEqualTo("/v1/messages");
        assertThat(req.getHeader("x-api-key")).isEqualTo("test-key");
        assertThat(req.getHeader("anthropic-version")).isEqualTo("2023-06-01");
        String body = req.getBody().readUtf8();
        assertThat(body).contains(MODEL).contains("warfarin").contains("ibuprofen");
    }

    @Test
    void explainInteraction_normalisesSeveritySynonyms() {
        server.enqueue(claudeResponse("{\"severity\": \"MODERATE\", \"summary\": \"mittel\"}"));
        LlmVerdict verdict = service.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb"));
        assertThat(verdict.severity()).isEqualTo(Severity.MEDIUM);
        assertThat(verdict.parsed()).isTrue();
    }

    @Test
    void explainInteraction_invalidJsonFallsBackToUnknown() {
        server.enqueue(claudeResponse("Sorry, I cannot help with medical advice."));

        LlmVerdict verdict = service.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb"));

        assertThat(verdict.severity()).isEqualTo(Severity.UNKNOWN);
        assertThat(verdict.parsed()).isFalse();
        assertThat(verdict.summary()).contains("Sorry");
    }

    @Test
    void explainInteraction_extractsJsonEmbeddedInProse() {
        server.enqueue(claudeResponse(
                "Sure — here you go:\n{\"severity\":\"low\",\"summary\":\"unbedenklich\"}\nlet me know."));

        LlmVerdict verdict = service.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb"));

        assertThat(verdict.severity()).isEqualTo(Severity.LOW);
        assertThat(verdict.summary()).isEqualTo("unbedenklich");
        assertThat(verdict.parsed()).isTrue();
    }

    @Test
    void explainInteraction_http500RaisesAnthropicApiException() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("{\"error\":\"boom\"}"));

        assertThatThrownBy(() -> service.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb")))
                .isInstanceOf(AnthropicApiException.class)
                .hasMessageContaining("500");
    }

    @Test
    void summarizeSideEffects_returnsPlainText() {
        server.enqueue(claudeResponse("Kann zu Magenblutungen führen. Bitte mit Arzt sprechen."));

        String summary = service.summarizeSideEffects("ibuprofen", "GI bleeding risk...");

        assertThat(summary).startsWith("Kann zu Magenblutungen");
    }

    @Test
    void callWithoutApiKeyIsRejected() {
        var webClient = new WebClientConfig()
                .anthropicWebClient(server.url("/").toString(), "", 5);
        AnthropicService unconfigured = new AnthropicService(
                webClient, new ObjectMapper(), MODEL, 256, "");

        assertThat(unconfigured.isConfigured()).isFalse();
        assertThatThrownBy(() -> unconfigured.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb")))
                .isInstanceOf(AnthropicApiException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void requestTimesOutOnSlowServer() {
        var webClient = new WebClientConfig()
                .anthropicWebClient(server.url("/").toString(), "test-key", 1);
        AnthropicService fast = new AnthropicService(
                webClient, new ObjectMapper(), MODEL, 256, "test-key");

        server.enqueue(claudeResponse("{\"severity\":\"LOW\",\"summary\":\"...\"}")
                .setBodyDelay(3, TimeUnit.SECONDS));

        assertThatThrownBy(() -> fast.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb")))
                .isInstanceOf(AnthropicApiException.class);
    }

    private MockResponse claudeResponse(String text) {
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "id": "msg_test",
                          "model": "%s",
                          "stop_reason": "end_turn",
                          "content": [{"type":"text","text":"%s"}]
                        }
                        """.formatted(MODEL, escaped));
    }
}
