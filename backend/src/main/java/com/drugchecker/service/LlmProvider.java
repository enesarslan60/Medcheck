package com.drugchecker.service;

import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.model.SupportedLanguage;

import java.util.List;

/**
 * Provider-agnostic contract for the LLM analysis layer.
 *
 * <p>Concrete implementations wrap a specific vendor (Anthropic, Google
 * Gemini, a local Ollama, ...). {@link InteractionService} depends only
 * on this interface, so switching providers is a config change.
 */
public interface LlmProvider {

    /** @return {@code true} if the provider has been configured with an API key. */
    boolean isConfigured();

    /**
     * Ask the LLM for a severity classification + short explanation of the
     * interaction between the given drugs, using their openFDA
     * drug_interactions texts as the sole source of truth.
     *
     * @param language target language for the {@code summary} field
     */
    LlmVerdict explainInteraction(List<String> drugNames,
                                  List<String> drugTexts,
                                  SupportedLanguage language);

    /**
     * Ask the LLM for a short summary of the important warnings for a
     * single drug based on its openFDA drug_interactions text.
     *
     * @param language target language for the summary text
     */
    String summarizeSideEffects(String drugName,
                                String labelText,
                                SupportedLanguage language);
}
