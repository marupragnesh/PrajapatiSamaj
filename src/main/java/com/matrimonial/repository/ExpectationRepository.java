package com.matrimonial.repository;

import com.matrimonial.entity.Expectation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY: ExpectationRepository
 *
 * Handles all DB operations for the "expectations" table.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface ExpectationRepository extends JpaRepository<Expectation, Long> {

    // Find expectation by user ID
    Optional<Expectation> findByUserId(Long userId);
}
