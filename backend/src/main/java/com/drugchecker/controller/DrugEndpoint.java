package com.drugchecker.controller;

import com.drugchecker.dto.DrugDTO;
import com.drugchecker.dto.RxNormCandidate;
import com.drugchecker.service.DrugService;
import com.drugchecker.service.RxNormService;
import com.drugchecker.validation.DrugValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/drugs")
public class DrugEndpoint {

    private final DrugService drugService;
    private final RxNormService rxNormService;
    private final DrugValidator validator;

    public DrugEndpoint(DrugService drugService,
                        RxNormService rxNormService,
                        DrugValidator validator) {
        this.drugService = drugService;
        this.rxNormService = rxNormService;
        this.validator = validator;
    }

    /** Local dev/testing only — returns the drugs seeded in {@code data.sql}. */
    @GetMapping
    public ResponseEntity<List<DrugDTO>> getAll() {
        return ResponseEntity.ok(drugService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrugDTO> getById(@PathVariable Long id) {
        validator.validateDrugId(id);
        return ResponseEntity.ok(drugService.findById(id));
    }

    /**
     * Primary autocomplete source: fuzzy free-text lookup via RxNorm.
     * Replaces the previous H2 repository search.
     */
    @GetMapping("/search")
    public ResponseEntity<List<RxNormCandidate>> search(@RequestParam String name) {
        validator.validateDrugName(name);
        return ResponseEntity.ok(rxNormService.searchDrugs(name.trim()));
    }
}
