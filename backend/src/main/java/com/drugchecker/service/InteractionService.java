package com.drugchecker.service;

import com.drugchecker.dto.InteractionDTO;
import com.drugchecker.model.Drug;
import com.drugchecker.model.Interaction;
import com.drugchecker.repository.InteractionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final DrugService drugService;
    private final OpenFDAService openFDAService;
    private final LLMService llmService;

    public InteractionService(InteractionRepository interactionRepository,
                              DrugService drugService,
                              OpenFDAService openFDAService,
                              LLMService llmService) {
        this.interactionRepository = interactionRepository;
        this.drugService = drugService;
        this.openFDAService = openFDAService;
        this.llmService = llmService;
    }

    public List<InteractionDTO> checkInteractions(List<String> drugNames) {
        List<InteractionDTO> results = new ArrayList<>();
        if (drugNames == null || drugNames.size() < 2) {
            return results;
        }

        List<String> substances = drugNames.stream()
                .map(this::resolveSubstance)
                .toList();

        for (int i = 0; i < substances.size(); i++) {
            for (int j = i + 1; j < substances.size(); j++) {
                String s1 = substances.get(i);
                String s2 = substances.get(j);
                results.add(checkPair(s1, s2));
            }
        }
        return results;
    }

    private InteractionDTO checkPair(String s1, String s2) {
        // 1. Check local cache (H2)
        var cached = interactionRepository.findByDrugPair(s1, s2);
        if (cached.isPresent()) {
            return InteractionDTO.fromEntity(cached.get(), "LOCAL");
        }

        // 2. Try OpenFDA
        String fdaText = openFDAService.searchInteractions(s1, s2);
        if (fdaText == null || fdaText.isBlank()) {
            return InteractionDTO.notFound(s1, s2);
        }

        // 3. Generate plain-language explanation
        String explanation = llmService.generateExplanation(s1, s2, fdaText);
        String severity = inferSeverity(fdaText);

        Interaction saved = new Interaction();
        saved.setDrug1(s1);
        saved.setDrug2(s2);
        saved.setSeverity(severity);
        saved.setDescription(truncate(fdaText, 3900));
        saved.setLlmExplanation(truncate(explanation, 3900));

        // 4. Cache for future lookups
        Interaction persisted = interactionRepository.save(saved);
        return InteractionDTO.fromEntity(persisted, "OPENFDA");
    }

    private String resolveSubstance(String input) {
        Drug drug = drugService.resolveByNameOrSubstance(input);
        return drug != null ? drug.getActiveSubstance() : input;
    }

    private String inferSeverity(String text) {
        String lower = text.toLowerCase();
        if (lower.contains("contraindicated") || lower.contains("severe")
                || lower.contains("life-threatening") || lower.contains("fatal")) {
            return "SEVERE";
        }
        if (lower.contains("avoid") || lower.contains("caution") || lower.contains("monitor")) {
            return "MODERATE";
        }
        return "MILD";
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
