package com.matrimonial.repository;

import com.matrimonial.entity.Profile;
import com.matrimonial.entity.Profile.Gender;
import com.matrimonial.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY: ProfileRepository
 *
 * Handles all DB operations for the "profiles" table.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    // Find a user's own profile by User entity
    Optional<Profile> findByUser(User user);

    // Find a user's own profile by userId
    Optional<Profile> findByUserId(Long userId);

    // Check if user already has a profile
    boolean existsByUserId(Long userId);

    /**
     * Discovery query — find all COMPLETE profiles matching gender preference,
     * excluding the logged-in user's own profile.
     *
     * Used in DiscoverController → DiscoverService → here.
     *
     * @param gender   the preferred gender to filter by
     * @param userId   the current user's ID (excluded from results)
     * @param pageable pagination info (page number + size)
     */
    @Query("SELECT p FROM Profile p WHERE p.gender = :gender AND p.user.id != :userId AND p.isComplete = true")
    Page<Profile> findByGenderAndUserIdNotAndIsCompleteTrue(
            @Param("gender") Gender gender,
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Discovery query when preference is ANY (both genders shown).
     * Excludes the current user's profile.
     */
    @Query("SELECT p FROM Profile p WHERE p.user.id != :userId AND p.isComplete = true")
    Page<Profile> findByUserIdNotAndIsCompleteTrue(
            @Param("userId") Long userId,
            Pageable pageable
    );
}
