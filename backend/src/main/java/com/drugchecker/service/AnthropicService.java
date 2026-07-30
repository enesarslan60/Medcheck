package com.drugchecker.service;

import com.drugchecker.dto.anthropic.AnthropicMessage;
import com.drugchecker.dto.anthropic.AnthropicMessagesRequest;
import com.drugchecker.dto.anthropic.AnthropicMessagesResponse;
import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.exception.AnthropicApiException;
import com.drugchecker.model.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WebClient-based wrapper around the Anthropic Messages API.
 * All prompt texts live in {@link AnthropicPrompts} for easy reference
 * from the thesis prompt-engineering section.
 */
@Service
public class AnthropicService {

    private static final Logger log = LoggerFactory.getLogger(AnthropicService.class);

    /** Locates the first {"..."} block in a text — helps when the model wraps JSON in prose. */
    private static final Pattern JSON_OBJECT_PATTERN = Pattern.compile("\\{[\\s\\S]*\\}");

    private final WebClient anthropicWebClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;
    private final boolean configured;

    public AnthropicService(@Qualifier("anthropicWebClient") WebClient anthropicWebClient,
                            ObjectMapper objectMapper,
                            @Value("${anthropic.model}") String model,
                            @Value("${anthropic.max-tokens:512}") int maxTokens,
                            @Value("${anthropic.api-key:}") String apiKey) {
        this.anthropicWebClient = anthropicWebClient;
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxTokens = maxTokens;
        this.configured = apiKey != null && !apiKey.isBlank();
    }

    /** Whether the service has been configured with an API key. */
    public boolean isConfigured() {
        return configured;
    }

    /**
     * Ask Claude for a severity classification and a short German explanation
     * of the interaction between the given drugs, using their openFDA
     * drug_interactions texts as the sole source of truth.
     *
     * @throws AnthropicApiException on transport / API errors
     */
    public LlmVerdict explainInteraction(List<String> drugNames, List<String> drugTexts) {
        String userMessage = AnthropicPrompts.interactionUserMessage(drugNames, drugTexts);
        String rawResponse = callMessages(AnthropicPrompts.INTERACTION_SYSTEM_PROMPT, userMessage);
        return parseVerdict(rawResponse);
    }

    /**
     * Ask Claude for a short German summary of the important warnings for a
     * single drug based on its openFDA drug_interactions text.
     *
     * @throws AnthropicApiException on transport / API errors
     */
    public String summarizeSideEffects(String drugName, String labelText) {
        String userMessage = AnthropicPrompts.sideEffectsUserMessage(drugName, labelText);
        String raw = callMessages(AnthropicPrompts.SIDE_EFFECTS_SYSTEM_PROMPT, userMessage);
        return raw == null ? "" : raw.trim();
    }

    // ---------------------------------------------------------------------
    // Internals
    // ---------------------------------------------------------------------

    private String callMessages(String systemPrompt, String userMessage) {
        if (!configured) {
            throw new AnthropicApiException("Anthropic API key is not configured");
        }
        AnthropicMessagesRequest body = new AnthropicMessagesRequest(
                model,
                maxTokens,
                systemPrompt,
                List.of(new AnthropicMessage("user", userMessage))
        );
        try {
            AnthropicMessagesResponse response = anthropicWebClient.post()
                    .uri("/v1/messages")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(AnthropicMessagesResponse.class)
                    .block();
            if (response == null) {
                throw new AnthropicApiException("Empty response from Anthropic");
            }
            return response.firstText();
        } catch (WebClientResponseException ex) {
            log.warn("Anthropic returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AnthropicApiException(
                    "Anthropic request failed: " + ex.getStatusCode(), ex);
        } catch (RuntimeException ex) {
            throw new AnthropicApiException("Anthropic request failed", ex);
        }
    }

    /**
     * Parse the JSON verdict the model was asked to return. If parsing fails
     * we fall back to {@link Severity#UNKNOWN} and pass the raw text through
     * so the caller can decide how to present it.
     */
    LlmVerdict parseVerdict(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return LlmVerdict.fallback("");
        }
        String candidate = extractJsonObject(rawResponse);
        if (candidate == null) {
            log.debug("Model response did not contain a JSON object; falling back. Body: {}", rawResponse);
            return LlmVerdict.fallback(rawResponse.trim());
        }
        try {
            JsonNode node = objectMapper.readTree(candidate);
            String severityText = node.hasNonNull("severity") ? node.get("severity").asText() : null;
            String summary = node.hasNonNull("summary") ? node.get("summary").asText() : null;
            Severity severity = Severity.fromString(severityText);
            if (summary == null || summary.isBlank()) {
                return LlmVerdict.fallback(rawResponse.trim());
            }
            return new LlmVerdict(severity, summary.trim(), true);
        } catch (Exception ex) {
            log.debug("Failed to parse LLM JSON verdict, using fallback. Cause: {}", ex.getMessage());
            return LlmVerdict.fallback(rawResponse.trim());
        }
    }

    private String extractJsonObject(String text) {
        Matcher m = JSON_OBJECT_PATTERN.matcher(text);
        return m.find() ? m.group() : null;
    }
}
