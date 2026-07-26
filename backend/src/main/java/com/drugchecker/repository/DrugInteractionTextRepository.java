package com.drugchecker.repository;

import com.drugchecker.model.DrugInteractionText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrugInteractionTextRepository extends JpaRepository<DrugInteractionText, Long> {

    Optional<DrugInteractionText> findByCacheKeyIgnoreCase(String cacheKey);
}
