package com.matrimonial.repository;

import com.matrimonial.entity.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * REPOSITORY: PhotoRepository
 *
 * Handles all DB operations for the "profile_photos" table.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface PhotoRepository extends JpaRepository<ProfilePhoto, Long> {

    // Get all photos of a profile — used to check 5-photo limit
    List<ProfilePhoto> findByProfileId(Long profileId);

    // Count how many photos a profile currently has
    int countByProfileId(Long profileId);
}
