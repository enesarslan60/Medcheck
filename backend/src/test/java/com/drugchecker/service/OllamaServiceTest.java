package com.drugchecker.service;

import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.exception.OllamaApiException;
import com.drugchecker.model.Severity;
import com.drugchecker.model.SupportedLanguage;
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

class OllamaServiceTest {

    private static final String MODEL = "llama-test-model";

    private MockWebServer server;
    private OllamaService service;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        service = new OllamaService(buildClient(5), new ObjectMapper(), MODEL, 256);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    @Test
    void isConfigured_returnsTrueBecauseNoKeyIsNeeded() {
        assertThat(service.isConfigured()).isTrue();
    }

    @Test
    void explainInteraction_parsesStructuredVerdict() throws InterruptedException {
        server.enqueue(ollamaResponse("""
                {
                  "severity": "HIGH",
                  "summary": "Die Kombination erhöht das Blutungsrisiko."
                }
                """));

        LlmVerdict verdict = service.explainInteraction(
                List.of("warfarin", "ibuprofen"),
                List.of("warfarin text", "ibuprofen text"), SupportedLanguage.GERMAN);

        assertThat(verdict.severity()).isEqualTo(Severity.HIGH);
        assertThat(verdict.summary()).contains("Blutungsrisiko");
        assertThat(verdict.parsed()).isTrue();

        RecordedRequest req = server.takeRequest();
        assertThat(req.getPath()).isEqualTo("/api/chat");
        String body = req.getBody().readUtf8();
        assertThat(body).contains(MODEL).contains("\"stream\":false")
                .contains("\"role\":\"system\"").contains("\"role\":\"user\"");
    }

    @Test
    void explainInteraction_invalidJsonFallsBackToUnknown() {
        server.enqueue(ollamaResponse("Sorry, I cannot help with medical advice."));

        LlmVerdict verdict = service.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb"), SupportedLanguage.GERMAN);

        assertThat(verdict.severity()).isEqualTo(Severity.UNKNOWN);
        assertThat(verdict.parsed()).isFalse();
    }

    @Test
    void explainInteraction_http500RaisesOllamaApiException() {
        server.enqueue(new MockResponse().setResponseCode(500).setBody("model not found"));

        assertThatThrownBy(() -> service.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb"), SupportedLanguage.GERMAN))
                .isInstanceOf(OllamaApiException.class)
                .hasMessageContaining("500");
    }

    @Test
    void summarizeSideEffects_returnsPlainText() {
        server.enqueue(ollamaResponse("Kann zu Magenblutungen führen."));

        String summary = service.summarizeSideEffects("ibuprofen", "GI bleeding risk...", SupportedLanguage.GERMAN);

        assertThat(summary).startsWith("Kann zu Magenblutungen");
    }

    @Test
    void requestTimesOutOnSlowServer() {
        OllamaService fast = new OllamaService(buildClient(1), new ObjectMapper(), MODEL, 256);
        server.enqueue(ollamaResponse("{\"severity\":\"LOW\",\"summary\":\"...\"}")
                .setBodyDelay(3, TimeUnit.SECONDS));

        assertThatThrownBy(() -> fast.explainInteraction(
                List.of("a", "b"), List.of("aa", "bb"), SupportedLanguage.GERMAN))
                .isInstanceOf(OllamaApiException.class);
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

    private MockResponse ollamaResponse(String text) {
        String escaped = text.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        return new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {
                          "model": "%s",
                          "message": {"role": "assistant", "content": "%s"},
                          "done": true
                        }
                        """.formatted(MODEL, escaped));
    }
}
