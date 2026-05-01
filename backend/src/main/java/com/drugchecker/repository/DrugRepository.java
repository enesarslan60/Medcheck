package com.drugchecker.repository;

import com.drugchecker.model.Drug;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrugRepository extends JpaRepository<Drug, Long> {

    List<Drug> findByNameContainingIgnoreCaseOrActiveSubstanceContainingIgnoreCase(String name, String activeSubstance);

    Optional<Drug> findByNameIgnoreCase(String name);

    Optional<Drug> findByActiveSubstanceIgnoreCase(String activeSubstance);
}
