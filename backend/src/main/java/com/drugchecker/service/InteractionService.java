package com.drugchecker.service;

import com.drugchecker.dto.DrugInteractionData;
import com.drugchecker.dto.RxNormCandidate;
import com.drugchecker.model.DrugInteractionText;
import com.drugchecker.repository.DrugInteractionTextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Per-drug data fetching for the interaction checker.
 * <p>
 * For every drug the user selected we:
 * <ol>
 *   <li>Resolve the free-text name to a canonical RxCUI via {@link RxNormService}.</li>
 *   <li>Check the local {@link DrugInteractionText} cache.</li>
 *   <li>On a miss, fetch the openFDA drug_interactions text and persist it.</li>
 * </ol>
 * The LLM-based cross-drug analysis runs on top of this and is intentionally
 * kept out of this class.
 */
@Service
public class InteractionService {

    private static final Logger log = LoggerFactory.getLogger(InteractionService.class);
    private static final int MAX_TEXT_LENGTH = 7900;

    private final RxNormService rxNormService;
    private final OpenFDAService openFDAService;
    private final DrugInteractionTextRepository cacheRepository;

    public InteractionService(RxNormService rxNormService,
                              OpenFDAService openFDAService,
                              DrugInteractionTextRepository cacheRepository) {
        this.rxNormService = rxNormService;
        this.openFDAService = openFDAService;
        this.cacheRepository = cacheRepository;
    }

    public List<DrugInteractionData> checkInteractions(List<String> drugNames) {
        if (drugNames == null || drugNames.isEmpty()) {
            return List.of();
        }
        List<DrugInteractionData> results = new ArrayList<>(drugNames.size());
        for (String name : drugNames) {
            if (name == null || name.isBlank()) continue;
            results.add(fetchForDrug(name.trim()));
        }
        return results;
    }

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
        Optional<String> fetched = openFDAService.getDrugInteractionText(lookupKey);
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
            // Cache is best-effort — a duplicate insert from a parallel request is not fatal.
            log.debug("Cache write failed for {}: {}", cacheKey, e.getMessage());
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
