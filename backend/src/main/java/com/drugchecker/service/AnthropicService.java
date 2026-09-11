package com.drugchecker.service;

import com.drugchecker.dto.anthropic.AnthropicMessage;
import com.drugchecker.dto.anthropic.AnthropicMessagesRequest;
import com.drugchecker.dto.anthropic.AnthropicMessagesResponse;
import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.exception.AnthropicApiException;
import com.drugchecker.model.SupportedLanguage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

/**
 * WebClient-based wrapper around the Anthropic Messages API.
 * Active when {@code llm.provider=anthropic} (the default).
 * All prompt texts live in {@link AnthropicPrompts}.
 */
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "anthropic", matchIfMissing = true)
public class AnthropicService implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(AnthropicService.class);

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
        log.info("AnthropicService initialised (configured={}, model={})", configured, model);
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }

    @Override
    public LlmVerdict explainInteraction(List<String> drugNames,
                                         List<String> drugTexts,
                                         SupportedLanguage language) {
        String userMessage = AnthropicPrompts.interactionUserMessage(drugNames, drugTexts);
        String rawResponse = callMessages(
                AnthropicPrompts.interactionSystemPrompt(language), userMessage);
        return LlmParsing.parseVerdict(rawResponse, objectMapper);
    }

    @Override
    public String summarizeSideEffects(String drugName,
                                       String labelText,
                                       SupportedLanguage language) {
        String userMessage = AnthropicPrompts.sideEffectsUserMessage(drugName, labelText);
        String raw = callMessages(
                AnthropicPrompts.sideEffectsSystemPrompt(language), userMessage);
        return raw == null ? "" : raw.trim();
    }

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
}
