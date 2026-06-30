package com.drugchecker.service;

import com.drugchecker.dto.DrugDTO;
import com.drugchecker.exception.ResourceNotFoundException;
import com.drugchecker.model.Drug;
import com.drugchecker.repository.DrugRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DrugService {

    private final DrugRepository drugRepository;

    public DrugService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    public List<DrugDTO> findAll() {
        return drugRepository.findAll().stream()
                .map(DrugDTO::fromEntity)
                .toList();
    }

    public DrugDTO findById(Long id) {
        return drugRepository.findById(id)
                .map(DrugDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("Drug not found: id=" + id));
    }

    public List<DrugDTO> searchByName(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return drugRepository
                .findByNameContainingIgnoreCaseOrActiveSubstanceContainingIgnoreCase(query, query)
                .stream()
                .map(DrugDTO::fromEntity)
                .toList();
    }

    public Drug resolveByNameOrSubstance(String identifier) {
        return drugRepository.findByNameIgnoreCase(identifier)
                .or(() -> drugRepository.findByActiveSubstanceIgnoreCase(identifier))
                .orElse(null);
    }
}
