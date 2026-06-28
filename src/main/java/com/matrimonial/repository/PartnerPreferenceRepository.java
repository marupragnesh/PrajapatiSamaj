package com.matrimonial.repository;

import com.matrimonial.entity.PartnerPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY: PartnerPreferenceRepository
 *
 * Handles all DB operations for the "partner_preferences" table.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface PartnerPreferenceRepository extends JpaRepository<PartnerPreference, Long> {

    // Find preference by userId — used in discovery to know what gender to filter
    Optional<PartnerPreference> findByUserId(Long userId);

    // Check if preference already exists for user
    boolean existsByUserId(Long userId);
}
