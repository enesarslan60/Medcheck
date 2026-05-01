package com.drugchecker.dto;

import com.drugchecker.model.Drug;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DrugDTO {

    private Long id;
    private String name;
    private String activeSubstance;
    private String description;
    private String sideEffects;

    public static DrugDTO fromEntity(Drug drug) {
        return new DrugDTO(
                drug.getId(),
                drug.getName(),
                drug.getActiveSubstance(),
                drug.getDescription(),
                drug.getSideEffects()
        );
    }
}
