package com.drugchecker.service;

import com.drugchecker.dto.DrugDetail;
import com.drugchecker.dto.DrugInteractionData;
import com.drugchecker.dto.InteractionExplanationResponse;
import com.drugchecker.dto.RxNormCandidate;
import com.drugchecker.dto.anthropic.LlmVerdict;
import com.drugchecker.exception.AnthropicApiException;
import com.drugchecker.model.DrugInteractionText;
import com.drugchecker.model.Severity;
import com.drugchecker.repository.DrugInteractionTextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Orchestrates drug-interaction checking:
 * <ol>
 *   <li>Resolve each free-text drug name to an RxCUI via {@link RxNormService}.</li>
 *   <li>Fetch (and cache) the openFDA drug_interactions text per drug.</li>
 *   <li>Run the LLM analysis in parallel — one interaction verdict + N
 *       per-drug side-effect summaries — via {@link AnthropicService}.</li>
 *   <li>Gracefully degrade to raw-only data if the LLM step fails.</li>
 * </ol>
 */
@Service
public class InteractionService {

    private static final Logger log = LoggerFactory.getLogger(InteractionService.class);
    private static final int MAX_TEXT_LENGTH = 7900;

    private final RxNormService rxNormService;
    private final OpenFDAService openFDAService;
    private final DrugInteractionTextRepository cacheRepository;
    private final AnthropicService anthropicService;

    public InteractionService(RxNormService rxNormService,
                              OpenFDAService openFDAService,
                              DrugInteractionTextRepository cacheRepository,
                              AnthropicService anthropicService) {
        this.rxNormService = rxNormService;
        this.openFDAService = openFDAService;
        this.cacheRepository = cacheRepository;
        this.anthropicService = anthropicService;
    }

    public InteractionExplanationResponse checkInteractions(List<String> drugNames) {
        if (drugNames == null || drugNames.isEmpty()) {
            return new InteractionExplanationResponse(Severity.UNKNOWN, null, List.of(), false);
        }

        // 1. Raw data per drug (cached).
        List<DrugInteractionData> raw = new ArrayList<>(drugNames.size());
        for (String name : drugNames) {
            if (name == null || name.isBlank()) continue;
            raw.add(fetchForDrug(name.trim()));
        }
        if (raw.isEmpty()) {
            return new InteractionExplanationResponse(Severity.UNKNOWN, null, List.of(), false);
        }

        // 2. If Anthropic isn't configured, ship the raw data straight away.
        if (!anthropicService.isConfigured()) {
            log.info("Anthropic not configured — returning raw-only interaction data");
            return buildFallback(raw);
        }

        // 3. Fire LLM calls in parallel.
        List<String> names = raw.stream().map(DrugInteractionData::drugName).toList();
        List<String> texts = raw.stream().map(DrugInteractionData::interactionText).toList();

        CompletableFuture<LlmVerdict> verdictFuture =
                CompletableFuture.supplyAsync(() -> anthropicService.explainInteraction(names, texts));

        List<CompletableFuture<String>> summaryFutures = new ArrayList<>(raw.size());
        for (DrugInteractionData d : raw) {
            summaryFutures.add(CompletableFuture.supplyAsync(() ->
                    d.interactionText() == null || d.interactionText().isBlank()
                            ? null
                            : anthropicService.summarizeSideEffects(d.drugName(), d.interactionText())));
        }

        try {
            CompletableFuture<Void> all = CompletableFuture.allOf(
                    joinAll(verdictFuture, summaryFutures));
            all.get();
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("LLM orchestration interrupted");
            return buildFallback(raw);
        } catch (ExecutionException ee) {
            log.warn("LLM orchestration failed: {}", rootMessage(ee));
            return buildFallback(raw);
        }

        LlmVerdict verdict = getOrFallback(verdictFuture);
        List<DrugDetail> details = new ArrayList<>(raw.size());
        for (int i = 0; i < raw.size(); i++) {
            DrugInteractionData d = raw.get(i);
            String summary = getOrNull(summaryFutures.get(i));
            details.add(new DrugDetail(
                    d.drugName(), d.rxcui(), summary, d.interactionText(), d.source()));
        }

        return new InteractionExplanationResponse(
                verdict.severity(),
                verdict.summary(),
                details,
                verdict.parsed());
    }

    // ---------------------------------------------------------------------
    // Per-drug openFDA lookup + H2 cache
    // ---------------------------------------------------------------------

    private DrugInteractionData fetchForDrug(String userInput) {
        RxNormCandidate top = topCandidate(userInput);
        String rxcui = top != null ? top.rxcui() : null;
        String canonicalName = top != null ? top.name() : userInput;
        String cacheKey = rxcui != null ? rxcui : userInput.toLowerCase();

        Optional<DrugInteractionText> cached = cacheRepository.findByCacheKeyIgnoreCase(cacheKey);
        if (cached.isPresent()) {
            DrugInteractionText hit = cached.get();
            return new DrugInteractionData(
                    hit.getDrugName(), rxcui, hit.getInteractionText(), "LOCAL_CACHE");
        }

        String lookupKey = rxcui != null ? rxcui : userInput;
        Optional<String> fetched;
        try {
            fetched = openFDAService.getDrugInteractionText(lookupKey);
        } catch (RuntimeException ex) {
            log.warn("openFDA lookup for '{}' failed: {}", lookupKey, ex.getMessage());
            fetched = Optional.empty();
        }
        if (fetched.isEmpty()) {
            return DrugInteractionData.notFound(canonicalName, rxcui);
        }

        String text = truncate(fetched.get(), MAX_TEXT_LENGTH);
        String source = rxcui != null ? "OPENFDA_RXCUI" : "OPENFDA_GENERIC_NAME";
        persist(cacheKey, canonicalName, text, source);
        return new DrugInteractionData(canonicalName, rxcui, text, source);
    }

    private RxNormCandidate topCandidate(String userInput) {
        List<RxNormCandidate> candidates = rxNormService.searchDrugs(userInput, 1);
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    private void persist(String cacheKey, String drugName, String text, String source) {
        try {
            DrugInteractionText row = new DrugInteractionText();
            row.setCacheKey(cacheKey);
            row.setDrugName(drugName);
            row.setInteractionText(text);
            row.setSource(source);
            row.setFetchedAt(Instant.now());
            cacheRepository.save(row);
        } catch (RuntimeException e) {
            log.debug("Cache write failed for {}: {}", cacheKey, e.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // LLM orchestration helpers
    // ---------------------------------------------------------------------

    private InteractionExplanationResponse buildFallback(List<DrugInteractionData> raw) {
        List<DrugDetail> details = raw.stream()
                .map(d -> new DrugDetail(
                        d.drugName(), d.rxcui(), null, d.interactionText(), d.source()))
                .toList();
        return new InteractionExplanationResponse(Severity.UNKNOWN, null, details, false);
    }

    private LlmVerdict getOrFallback(CompletableFuture<LlmVerdict> future) {
        try {
            LlmVerdict v = future.getNow(null);
            return v != null ? v : LlmVerdict.fallback(null);
        } catch (RuntimeException e) {
            return LlmVerdict.fallback(null);
        }
    }

    private String getOrNull(CompletableFuture<String> future) {
        try {
            String v = future.getNow(null);
            return v == null || v.isBlank() ? null : v;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private CompletableFuture<?>[] joinAll(CompletableFuture<?> head, List<CompletableFuture<String>> tail) {
        CompletableFuture<?>[] arr = new CompletableFuture<?>[tail.size() + 1];
        arr[0] = head;
        for (int i = 0; i < tail.size(); i++) arr[i + 1] = tail.get(i);
        return arr;
    }

    private String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null && cur.getCause() != cur) cur = cur.getCause();
        return cur.getMessage();
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
