package com.drugchecker.dto;

import com.drugchecker.model.Drug;

public class DrugDTO {

    private Long id;
    private String name;
    private String activeSubstance;
    private String description;
    private String sideEffects;

    public DrugDTO() {
    }

    public DrugDTO(Long id, String name, String activeSubstance, String description, String sideEffects) {
        this.id = id;
        this.name = name;
        this.activeSubstance = activeSubstance;
        this.description = description;
        this.sideEffects = sideEffects;
    }

    public static DrugDTO fromEntity(Drug drug) {
        return new DrugDTO(
                drug.getId(),
                drug.getName(),
                drug.getActiveSubstance(),
                drug.getDescription(),
                drug.getSideEffects()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActiveSubstance() {
        return activeSubstance;
    }

    public void setActiveSubstance(String activeSubstance) {
        this.activeSubstance = activeSubstance;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSideEffects() {
        return sideEffects;
    }

    public void setSideEffects(String sideEffects) {
        this.sideEffects = sideEffects;
    }
}
