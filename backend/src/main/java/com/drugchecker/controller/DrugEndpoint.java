package com.drugchecker.controller;

import com.drugchecker.dto.DrugDTO;
import com.drugchecker.exception.ResourceNotFoundException;
import com.drugchecker.service.DrugService;
import com.drugchecker.service.OpenFDAService;
import com.drugchecker.validation.DrugValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drugs")
public class DrugEndpoint {

    private final DrugService drugService;
    private final OpenFDAService openFDAService;
    private final DrugValidator validator;

    public DrugEndpoint(DrugService drugService, OpenFDAService openFDAService, DrugValidator validator) {
        this.drugService = drugService;
        this.openFDAService = openFDAService;
        this.validator = validator;
    }

    @GetMapping
    public ResponseEntity<List<DrugDTO>> getAll() {
        return ResponseEntity.ok(drugService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DrugDTO> getById(@PathVariable Long id) {
        validator.validateDrugId(id);
        return ResponseEntity.ok(drugService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<DrugDTO>> search(@RequestParam String name) {
        validator.validateDrugName(name);
        return ResponseEntity.ok(drugService.searchByName(name.trim()));
    }

    @GetMapping("/openfda")
    public ResponseEntity<Map<String, Object>> openFda(@RequestParam String name) {
        validator.validateDrugName(name);
        Map<String, Object> info = openFDAService.searchDrugInfo(name.trim());
        if (info == null || info.isEmpty()) {
            throw new ResourceNotFoundException("No OpenFDA data found for drug: " + name);
        }
        return ResponseEntity.ok(info);
    }
}
