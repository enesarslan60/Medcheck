package com.drugchecker.dto;

import com.drugchecker.model.Interaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteractionDTO {

    private Long id;
    private String drug1;
    private String drug2;
    private String severity;
    private String description;
    private String llmExplanation;
    private String source;

    public static InteractionDTO fromEntity(Interaction interaction, String source) {
        return new InteractionDTO(
                interaction.getId(),
                interaction.getDrug1(),
                interaction.getDrug2(),
                interaction.getSeverity(),
                interaction.getDescription(),
                interaction.getLlmExplanation(),
                source
        );
    }

    public static InteractionDTO notFound(String drug1, String drug2) {
        InteractionDTO dto = new InteractionDTO();
        dto.setDrug1(drug1);
        dto.setDrug2(drug2);
        dto.setSeverity("NONE");
        dto.setDescription("No known interaction found between " + drug1 + " and " + drug2 + ".");
        dto.setLlmExplanation("No interaction data is available for this pair. Always consult a healthcare professional before combining medications.");
        dto.setSource("NONE");
        return dto;
    }
}
