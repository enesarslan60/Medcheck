package com.drugchecker.dto;

import com.drugchecker.model.Interaction;

public class InteractionDTO {

    private Long id;
    private String drug1;
    private String drug2;
    private String severity;
    private String description;
    private String llmExplanation;
    private String source;

    public InteractionDTO() {
    }

    public InteractionDTO(Long id, String drug1, String drug2, String severity,
                          String description, String llmExplanation, String source) {
        this.id = id;
        this.drug1 = drug1;
        this.drug2 = drug2;
        this.severity = severity;
        this.description = description;
        this.llmExplanation = llmExplanation;
        this.source = source;
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDrug1() {
        return drug1;
    }

    public void setDrug1(String drug1) {
        this.drug1 = drug1;
    }

    public String getDrug2() {
        return drug2;
    }

    public void setDrug2(String drug2) {
        this.drug2 = drug2;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLlmExplanation() {
        return llmExplanation;
    }

    public void setLlmExplanation(String llmExplanation) {
        this.llmExplanation = llmExplanation;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
