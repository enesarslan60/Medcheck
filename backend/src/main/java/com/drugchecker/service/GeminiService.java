package com.drugchecker.service;

import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.dto.gemini.GeminiContent;
import com.drugchecker.dto.gemini.GeminiGenerationConfig;
import com.drugchecker.dto.gemini.GeminiPart;
import com.drugchecker.dto.gemini.GeminiRequest;
import com.drugchecker.dto.gemini.GeminiResponse;
import com.drugchecker.dto.gemini.GeminiSystemInstruction;
import com.drugchecker.exception.GeminiApiException;
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
 * WebClient-based wrapper around Google's Gemini generateContent API.
 * Active when {@code llm.provider=gemini}. Reuses {@link AnthropicPrompts}
 * verbatim — the prompt content is provider-agnostic.
 *
 * <p>Gemini's free tier (1500 requests/day for Flash) makes this a good
 * zero-cost alternative to Anthropic during development / thesis demo.
 */
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "gemini")
public class GeminiService implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    private final WebClient geminiWebClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;
    private final String apiKey;
    private final boolean configured;

    public GeminiService(@Qualifier("geminiWebClient") WebClient geminiWebClient,
                         ObjectMapper objectMapper,
                         @Value("${gemini.model:gemini-2.5-flash}") String model,
                         @Value("${gemini.max-tokens:512}") int maxTokens,
                         @Value("${gemini.api-key:}") String apiKey) {
        this.geminiWebClient = geminiWebClient;
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxTokens = maxTokens;
        this.apiKey = apiKey == null ? "" : apiKey;
        this.configured = !this.apiKey.isBlank();
        log.info("GeminiService initialised (configured={}, model={})", configured, model);
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
        String raw = generate(AnthropicPrompts.interactionSystemPrompt(language), userMessage);
        return LlmParsing.parseVerdict(raw, objectMapper);
    }

    @Override
    public String summarizeSideEffects(String drugName,
                                       String labelText,
                                       SupportedLanguage language) {
        String userMessage = AnthropicPrompts.sideEffectsUserMessage(drugName, labelText);
        String raw = generate(AnthropicPrompts.sideEffectsSystemPrompt(language), userMessage);
        return raw == null ? "" : raw.trim();
    }

    private String generate(String systemPrompt, String userMessage) {
        if (!configured) {
            throw new GeminiApiException("Gemini API key is not configured");
        }
        GeminiRequest body = new GeminiRequest(
                List.of(new GeminiContent("user", List.of(new GeminiPart(userMessage)))),
                new GeminiSystemInstruction(List.of(new GeminiPart(systemPrompt))),
                new GeminiGenerationConfig(maxTokens, 0.2)
        );
        try {
            GeminiResponse response = geminiWebClient.post()
                    .uri(uri -> uri.path("/v1beta/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();
            if (response == null) {
                throw new GeminiApiException("Empty response from Gemini");
            }
            return response.firstText();
        } catch (WebClientResponseException ex) {
            log.warn("Gemini returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new GeminiApiException(
                    "Gemini request failed: " + ex.getStatusCode(), ex);
        } catch (RuntimeException ex) {
            throw new GeminiApiException("Gemini request failed", ex);
        }
    }
}
