package com.drugchecker.service;

import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.dto.ollama.OllamaChatMessage;
import com.drugchecker.dto.ollama.OllamaChatOptions;
import com.drugchecker.dto.ollama.OllamaChatRequest;
import com.drugchecker.dto.ollama.OllamaChatResponse;
import com.drugchecker.exception.OllamaApiException;
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
 * WebClient-based wrapper around a local Ollama instance's /api/chat
 * endpoint. Active when {@code llm.provider=ollama}.
 *
 * <p>No API key is needed — Ollama assumes trust on localhost. The provider
 * is considered "configured" as long as a base URL exists (default:
 * http://localhost:11434); if Ollama isn't actually running there, the
 * first call fails and the InteractionService falls back to raw data.
 *
 * <p>Reuses {@link AnthropicPrompts} verbatim — prompts are provider-agnostic.
 */
@Service
@ConditionalOnProperty(name = "llm.provider", havingValue = "ollama")
public class OllamaService implements LlmProvider {

    private static final Logger log = LoggerFactory.getLogger(OllamaService.class);

    private final WebClient ollamaWebClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final int maxTokens;

    public OllamaService(@Qualifier("ollamaWebClient") WebClient ollamaWebClient,
                         ObjectMapper objectMapper,
                         @Value("${ollama.model:llama3.1:8b}") String model,
                         @Value("${ollama.max-tokens:512}") int maxTokens) {
        this.ollamaWebClient = ollamaWebClient;
        this.objectMapper = objectMapper;
        this.model = model;
        this.maxTokens = maxTokens;
        log.info("OllamaService initialised (model={})", model);
    }

    @Override
    public boolean isConfigured() {
        // Ollama needs no API key; the actual availability is checked at call time.
        return true;
    }

    @Override
    public LlmVerdict explainInteraction(List<String> drugNames, List<String> drugTexts) {
        String userMessage = AnthropicPrompts.interactionUserMessage(drugNames, drugTexts);
        String raw = chat(AnthropicPrompts.INTERACTION_SYSTEM_PROMPT, userMessage);
        return LlmParsing.parseVerdict(raw, objectMapper);
    }

    @Override
    public String summarizeSideEffects(String drugName, String labelText) {
        String userMessage = AnthropicPrompts.sideEffectsUserMessage(drugName, labelText);
        String raw = chat(AnthropicPrompts.SIDE_EFFECTS_SYSTEM_PROMPT, userMessage);
        return raw == null ? "" : raw.trim();
    }

    private String chat(String systemPrompt, String userMessage) {
        OllamaChatRequest body = new OllamaChatRequest(
                model,
                false,
                List.of(
                        new OllamaChatMessage("system", systemPrompt),
                        new OllamaChatMessage("user", userMessage)
                ),
                new OllamaChatOptions(0.2, maxTokens)
        );
        try {
            OllamaChatResponse response = ollamaWebClient.post()
                    .uri("/api/chat")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(OllamaChatResponse.class)
                    .block();
            if (response == null) {
                throw new OllamaApiException("Empty response from Ollama");
            }
            return response.text();
        } catch (WebClientResponseException ex) {
            log.warn("Ollama returned {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new OllamaApiException(
                    "Ollama request failed: " + ex.getStatusCode(), ex);
        } catch (RuntimeException ex) {
            throw new OllamaApiException("Ollama request failed", ex);
        }
    }
}
