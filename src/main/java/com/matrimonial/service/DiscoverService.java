package com.matrimonial.service;

import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.dto.response.ProfileSearchResultDto;
import com.matrimonial.entity.*;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.mapper.ProfileMapper;
import com.matrimonial.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE: DiscoverService
 *
 * Handles the discovery / browse profiles feature and name-based search.
 *
 * Business rules:
 *   - discoverProfiles: only complete profiles, filtered by gender preference,
 *     excluding logged-in user, sorted newest first, paginated.
 *   - searchByName: case-insensitive partial name match across all complete
 *     profiles (excluding logged-in user). Returns lightweight DTO (name + DP).
 *
 * Entity → DTO conversion delegated to ProfileMapper (single source of truth).
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DiscoverService {

    private final ProfileRepository profileRepository;
    private final PartnerPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final ProfileMapper profileMapper;

    /**
     * Get a paginated list of profiles matching the logged-in user's preference.
     *
     * @param email  logged-in user's email
     * @param page   page number (0-indexed)
     * @param size   number of profiles per page
     */
    public List<ProfileResponse> discoverProfiles(String email, int page, int size) {
        User currentUser = getUserByEmail(email);

        PartnerPreference preference = preferenceRepository.findByUserId(currentUser.getId())
                .orElse(PartnerPreference.builder()
                        .preferredGender(PartnerPreference.PreferredGender.ANY)
                        .build());

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Profile> profilePage;

        if (preference.getPreferredGender() == PartnerPreference.PreferredGender.ANY) {
            profilePage = profileRepository.findByUserIdNotAndIsCompleteTrue(
                    currentUser.getId(), pageable);
        } else {
            Profile.Gender genderFilter = Profile.Gender.valueOf(
                    preference.getPreferredGender().name());
            profilePage = profileRepository.findByGenderAndUserIdNotAndIsCompleteTrue(
                    genderFilter, currentUser.getId(), pageable);
        }

        // isOwnProfile = false — these are other users' profiles (mobile masked)
        return profilePage.getContent().stream()
                .map(profile -> profileMapper.toProfileResponse(profile, false))
                .collect(Collectors.toList());
    }

    /**
     * Search complete profiles by full name (case-insensitive, partial match).
     * Returns lightweight results: profileId + fullName + primaryPhotoUrl only.
     * Excludes the logged-in user from results.
     *
     * @param email   logged-in user's email
     * @param keyword partial or full name to search for
     */
    public List<ProfileSearchResultDto> searchByName(String email, String keyword) {
        User currentUser = getUserByEmail(email);

        // Limit to 20 results — search is meant for quick lookup, not full browse
        Pageable pageable = PageRequest.of(0, 20, Sort.by("fullName").ascending());

        Page<Profile> results = profileRepository.searchByFullNameContainingIgnoreCase(
                keyword.trim(), currentUser.getId(), pageable);

        List<ProfileSearchResultDto> list = results.getContent().stream()
                .map(profileMapper::toSearchResultDto)
                .collect(Collectors.toList());

        log.info("Search performed — keyword='{}', resultsCount={}, userId={}", keyword, list.size(), currentUser.getId());

        return list;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
