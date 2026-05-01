package com.drugchecker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LLMService {

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-sonnet-4-20250514";
    private static final String FALLBACK_MESSAGE =
            "Explanation currently unavailable. Please consult your doctor or pharmacist.";
    private static final String SYSTEM_PROMPT =
            "You are a medical assistant helping non-medical users understand drug interactions. " +
            "Explain the following drug interaction in simple, clear language. " +
            "Always recommend consulting a doctor for serious interactions. " +
            "Never invent medical facts — only explain what is provided to you.";

    private final RestTemplate restTemplate;
    private final String apiKey;

    public LLMService(RestTemplate restTemplate,
                      @Value("${anthropic.api.key:}") String apiKey) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    public String generateExplanation(String drug1, String drug2, String technicalDescription) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Anthropic API key not configured; returning fallback explanation.");
            return FALLBACK_MESSAGE;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-key", apiKey);
            headers.set("anthropic-version", "2023-06-01");

            String userMessage = String.format(
                    "Drug 1: %s%nDrug 2: %s%nTechnical description: %s",
                    drug1, drug2, technicalDescription);

            Map<String, Object> body = new HashMap<>();
            body.put("model", MODEL);
            body.put("max_tokens", 512);
            body.put("system", SYSTEM_PROMPT);
            body.put("messages", List.of(Map.of(
                    "role", "user",
                    "content", userMessage
            )));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(API_URL, request, Map.class);
            if (response == null) {
                return FALLBACK_MESSAGE;
            }

            Object content = response.get("content");
            if (content instanceof List<?> list && !list.isEmpty()) {
                Object first = list.get(0);
                if (first instanceof Map<?, ?> map) {
                    Object text = map.get("text");
                    if (text != null) {
                        return text.toString();
                    }
                }
            }
            return FALLBACK_MESSAGE;
        } catch (Exception e) {
            log.error("Anthropic API call failed: {}", e.getMessage());
            return FALLBACK_MESSAGE;
        }
    }
}
