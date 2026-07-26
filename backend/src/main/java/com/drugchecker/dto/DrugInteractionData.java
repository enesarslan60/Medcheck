package com.drugchecker.dto;

/**
 * One drug's raw interaction data as returned by the check endpoint.
 * The LLM-driven per-pair explanation runs on top of this and is not
 * part of the data-fetching layer.
 *
 * @param drugName        the name the user selected (typically the RxNorm candidate name)
 * @param rxcui           RxCUI resolved via RxNorm, may be null if resolution failed
 * @param interactionText openFDA section-7 free text, or null when no data was found
 * @param source          "LOCAL_CACHE", "OPENFDA_RXCUI", "OPENFDA_GENERIC_NAME", or "NOT_FOUND"
 */
public record DrugInteractionData(
        String drugName,
        String rxcui,
        String interactionText,
        String source
) {

    public static DrugInteractionData notFound(String drugName, String rxcui) {
        return new DrugInteractionData(drugName, rxcui, null, "NOT_FOUND");
    }
}
