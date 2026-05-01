package com.drugchecker.controller;

import com.drugchecker.dto.DrugDTO;
import com.drugchecker.service.DrugService;
import com.drugchecker.service.OpenFDAService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    private final DrugService drugService;
    private final OpenFDAService openFDAService;

    public DrugController(DrugService drugService, OpenFDAService openFDAService) {
        this.drugService = drugService;
        this.openFDAService = openFDAService;
    }

    @GetMapping
    public List<DrugDTO> getAll() {
        return drugService.findAll();
    }

    @GetMapping("/search")
    public List<DrugDTO> search(@RequestParam String name) {
        return drugService.searchByName(name);
    }

    @GetMapping("/openfda")
    public Map<String, Object> openFda(@RequestParam String name) {
        return openFDAService.searchDrugInfo(name);
    }
}
