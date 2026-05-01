package com.drugchecker.repository;

import com.drugchecker.model.Interaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    @Query("SELECT i FROM Interaction i WHERE " +
            "(LOWER(i.drug1) = LOWER(:d1) AND LOWER(i.drug2) = LOWER(:d2)) OR " +
            "(LOWER(i.drug1) = LOWER(:d2) AND LOWER(i.drug2) = LOWER(:d1))")
    Optional<Interaction> findByDrugPair(@Param("d1") String d1, @Param("d2") String d2);
}
