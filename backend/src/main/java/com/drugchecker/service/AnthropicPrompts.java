package com.drugchecker.service;

import com.drugchecker.model.SupportedLanguage;

import java.util.List;

/**
 * Central catalogue of the prompts sent to the LLM.
 *
 * <p>System prompts are kept in English because Claude / Gemini / Llama
 * follow English instructions most reliably; a {@link SupportedLanguage}
 * placeholder is injected into two spots per prompt to steer the target
 * language of the response.
 *
 * <p>All prompts are grounded in the exact openFDA text passed in — the
 * model is told not to invent medical facts and to defer to a doctor /
 * pharmacist when uncertain.
 */
public final class AnthropicPrompts {

    private AnthropicPrompts() {
    }

    // ---------------------------------------------------------------------
    // Base system prompts — {LANGUAGE} is replaced with the target language's
    // English name (e.g. "German", "Turkish") before the call.
    // ---------------------------------------------------------------------

    private static final String INTERACTION_SYSTEM_PROMPT_BASE = """
            You are a medical assistant helping non-medical users understand
            drug interactions.

            You will be given the drug_interactions section from openFDA drug
            labels for two or more medications. Analyse ONLY the text provided;
            do not invent facts, do not consult external knowledge, and do not
            give personal medical advice.

            Your job:
              1. Classify the overall interaction risk of combining the listed
                 drugs as one of LOW, MEDIUM, HIGH.
              2. Write a short explanation (2–4 sentences) for a layperson.

            Rules:
              - Answer STRICTLY in {LANGUAGE}, in plain, understandable language.
              - Be CONSERVATIVE: if the evidence is ambiguous, err on the
                higher severity. Never downgrade a clear warning.
              - Always tell the reader to consult a doctor or pharmacist for
                medical decisions.

            Output format — return ONLY one JSON object with exactly these two
            keys, no markdown fences, no comments, no extra prose:

              { "severity": "LOW" | "MEDIUM" | "HIGH",
                "summary": "..." }

            The value of "summary" must be written in {LANGUAGE}.
            """;

    private static final String SIDE_EFFECTS_SYSTEM_PROMPT_BASE = """
            You are a medical assistant helping non-medical users understand
            what a medication does.

            You will be given the openFDA drug_interactions text for a single
            medication. Summarise the most important warnings and side effects
            for a layperson in 2–4 short sentences in {LANGUAGE}.

            Rules:
              - Answer STRICTLY in {LANGUAGE}.
              - Use only information contained in the provided text.
              - Do not invent side effects or interactions.
              - Recommend consulting a doctor or pharmacist if unsure.

            Return the summary as plain text — no JSON, no markdown, no
            headings, just the sentences.
            """;

    /** System prompt for the pair-interaction analysis, targeted at {@code lang}. */
    public static String interactionSystemPrompt(SupportedLanguage lang) {
        return INTERACTION_SYSTEM_PROMPT_BASE.replace("{LANGUAGE}", lang.getEnglishName());
    }

    /** System prompt for the per-drug side-effects summary, targeted at {@code lang}. */
    public static String sideEffectsSystemPrompt(SupportedLanguage lang) {
        return SIDE_EFFECTS_SYSTEM_PROMPT_BASE.replace("{LANGUAGE}", lang.getEnglishName());
    }

    // ---------------------------------------------------------------------
    // User-message builders — identical across languages; the target
    // language is enforced solely via the system prompt.
    // ---------------------------------------------------------------------

    /** Builds the user message for an interaction explanation. */
    public static String interactionUserMessage(List<String> drugNames,
                                                List<String> drugTexts) {
        StringBuilder sb = new StringBuilder();
        sb.append("Analyse the interaction between the following medications:\n");
        for (int i = 0; i < drugNames.size(); i++) {
            sb.append("- ").append(drugNames.get(i)).append("\n");
        }
        sb.append("\nHere are the openFDA drug_interactions sections for each drug.\n");
        for (int i = 0; i < drugNames.size(); i++) {
            sb.append("\n=== ").append(drugNames.get(i)).append(" ===\n");
            String text = i < drugTexts.size() ? drugTexts.get(i) : null;
            sb.append(text != null && !text.isBlank()
                    ? text
                    : "(no openFDA drug_interactions text available)");
            sb.append("\n");
        }
        sb.append("\nReturn ONLY the JSON object as described in the system prompt.\n");
        return sb.toString();
    }

    /** Builds the user message for a single-drug side-effect summary. */
    public static String sideEffectsUserMessage(String drugName, String labelText) {
        StringBuilder sb = new StringBuilder();
        sb.append("Medication: ").append(drugName).append("\n\n");
        sb.append("openFDA drug_interactions section:\n");
        sb.append(labelText != null && !labelText.isBlank()
                ? labelText
                : "(no openFDA text available)");
        sb.append("\n\nWrite the summary now, plain text only.");
        return sb.toString();
    }
}
