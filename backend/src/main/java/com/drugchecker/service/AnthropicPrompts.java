package com.drugchecker.service;

import java.util.List;

/**
 * Central catalogue of the prompts sent to Claude.
 *
 * <p>System prompts are kept in English because Claude follows English
 * instructions most reliably; every prompt instructs the model to
 * <em>answer in German</em> so the end-user sees German output regardless
 * of the source data language.
 *
 * <p>All prompts are grounded in the exact openFDA text passed in — the
 * model is told not to invent medical facts and to defer to a doctor /
 * pharmacist when uncertain.
 */
public final class AnthropicPrompts {

    private AnthropicPrompts() {
    }

    // ---------------------------------------------------------------------
    // System prompts — verbatim, so they can be quoted in the thesis
    // ---------------------------------------------------------------------

    /** System prompt for {@link AnthropicService#explainInteraction}. */
    public static final String INTERACTION_SYSTEM_PROMPT = """
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
              - Answer STRICTLY in German, in plain, understandable language.
              - Be CONSERVATIVE: if the evidence is ambiguous, err on the
                higher severity. Never downgrade a clear warning.
              - Always tell the reader to consult a doctor or pharmacist for
                medical decisions.

            Output format — return ONLY one JSON object with exactly these two
            keys, no markdown fences, no comments, no extra prose:

              { "severity": "LOW" | "MEDIUM" | "HIGH",
                "summary": "..." }
            """;

    /** System prompt for {@link AnthropicService#summarizeSideEffects}. */
    public static final String SIDE_EFFECTS_SYSTEM_PROMPT = """
            You are a medical assistant helping non-medical users understand
            what a medication does.

            You will be given the openFDA drug_interactions text for a single
            medication. Summarise the most important warnings and side effects
            for a layperson in 2–4 short German sentences.

            Rules:
              - Answer STRICTLY in German.
              - Use only information contained in the provided text.
              - Do not invent side effects or interactions.
              - Recommend consulting a doctor or pharmacist if unsure.

            Return the summary as plain text — no JSON, no markdown, no
            headings, just the sentences.
            """;

    // ---------------------------------------------------------------------
    // User-message builders — keep the wire format identical to what the
    // model sees so it can be reproduced 1:1 in the thesis.
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
        sb.append("\n\nWrite the German summary now, plain text only.");
        return sb.toString();
    }
}
