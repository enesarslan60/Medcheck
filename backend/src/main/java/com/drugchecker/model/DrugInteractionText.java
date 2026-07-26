package com.drugchecker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

/**
 * Per-drug cache for the openFDA "drug_interactions" free-text.
 * Keyed by RxCUI (or generic name when the RxCUI lookup fell back).
 */
@Entity
@Table(name = "drug_interaction_texts",
        uniqueConstraints = @UniqueConstraint(columnNames = "cache_key"))
public class DrugInteractionText {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** rxcui if known, otherwise the generic name that hit openFDA */
    @Column(name = "cache_key", nullable = false, length = 200)
    private String cacheKey;

    @Column(nullable = false, length = 200)
    private String drugName;

    @Column(nullable = false, length = 8000)
    private String interactionText;

    /** "OPENFDA_RXCUI" or "OPENFDA_GENERIC_NAME" */
    @Column(nullable = false, length = 40)
    private String source;

    @Column(nullable = false)
    private Instant fetchedAt;

    public DrugInteractionText() {
    }

    public DrugInteractionText(Long id, String cacheKey, String drugName,
                               String interactionText, String source, Instant fetchedAt) {
        this.id = id;
        this.cacheKey = cacheKey;
        this.drugName = drugName;
        this.interactionText = interactionText;
        this.source = source;
        this.fetchedAt = fetchedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCacheKey() { return cacheKey; }
    public void setCacheKey(String cacheKey) { this.cacheKey = cacheKey; }

    public String getDrugName() { return drugName; }
    public void setDrugName(String drugName) { this.drugName = drugName; }

    public String getInteractionText() { return interactionText; }
    public void setInteractionText(String interactionText) { this.interactionText = interactionText; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }
}
