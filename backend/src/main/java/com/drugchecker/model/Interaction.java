package com.drugchecker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "interactions")
public class Interaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String drug1;

    @Column(nullable = false)
    private String drug2;

    @Column(nullable = false)
    private String severity;

    @Column(length = 4000)
    private String description;

    @Column(length = 4000)
    private String llmExplanation;

    public Interaction() {
    }

    public Interaction(Long id, String drug1, String drug2, String severity,
                       String description, String llmExplanation) {
        this.id = id;
        this.drug1 = drug1;
        this.drug2 = drug2;
        this.severity = severity;
        this.description = description;
        this.llmExplanation = llmExplanation;
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
}
