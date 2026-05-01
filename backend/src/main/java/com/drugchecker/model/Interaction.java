package com.drugchecker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
