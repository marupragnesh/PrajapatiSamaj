package com.matrimonial.service;

import com.matrimonial.dto.request.DiscoverFilterRequest;
import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.dto.response.ProfileSearchResultDto;
import com.matrimonial.entity.*;
import com.matrimonial.entity.enums.PaymentFeature;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.mapper.ProfileMapper;
import com.matrimonial.repository.*;
import com.matrimonial.repository.specification.ProfileSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE: DiscoverService
 *
 * Handles the discovery / browse profiles feature with dynamic filters and name-based search.
 *
 * Business rules:
 *   - discoverProfiles: only complete profiles, filtered by gender preference and optional filters
 *     (age, marital status, height, diet, surname), excluding logged-in user, sorted newest first, paginated.
 *   - Discover filters require PaymentFeature.DISCOVER_FILTERS unlock.
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
    private final PaymentService paymentService;

    /**
     * Get a paginated list of profiles matching the logged-in user's preference and optional filters.
     *
     * @param email  logged-in user's email
     * @param page   page number (0-indexed)
     * @param size   number of profiles per page
     * @param filter optional search filters (minAge, maxAge, maritalStatus, minHeight, maxHeight, diet)
     */
    public List<ProfileResponse> discoverProfiles(String email, int page, int size, DiscoverFilterRequest filter) {
        User currentUser = getUserByEmail(email);

        if (filter != null && hasActiveFilters(filter)) {
            boolean isUnlocked = paymentService.hasUnlocked(currentUser, PaymentFeature.DISCOVER_FILTERS)
                    || paymentService.hasUnlocked(currentUser, PaymentFeature.CONTACT_UNLOCK);
            if (!isUnlocked) {
                throw new BadRequestException("Discover filters are a premium feature. Please upgrade your plan.");
            }
        }

        PartnerPreference preference = preferenceRepository.findByUserId(currentUser.getId())
                .orElse(PartnerPreference.builder()
                        .preferredGender(PartnerPreference.PreferredGender.ANY)
                        .build());

        Profile.Gender genderFilter = null;
        if (preference.getPreferredGender() != PartnerPreference.PreferredGender.ANY) {
            genderFilter = Profile.Gender.valueOf(preference.getPreferredGender().name());
        }

        Specification<Profile> spec = ProfileSpecification.getDiscoverSpecification(
                currentUser.getId(), genderFilter, filter);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Profile> profilePage = profileRepository.findAll(spec, pageable);

        log.info("Discover profiles requested — userId={}, page={}, size={}, filter={}, resultCount={}",
                currentUser.getId(), page, size, filter, profilePage.getNumberOfElements());

        // isOwnProfile = false — these are other users' profiles. currentUser is
        // passed as viewer so ProfileMapper can check CONTACT_UNLOCK and show
        // full mobile numbers if this user has paid (account-wide unlock).
        return profilePage.getContent().stream()
                .map(profile -> profileMapper.toProfileResponse(profile, false, currentUser))
                .collect(Collectors.toList());
    }

    public List<ProfileResponse> discoverProfiles(String email, int page, int size) {
        return discoverProfiles(email, page, size, null);
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

    private boolean hasActiveFilters(DiscoverFilterRequest f) {
        return f.getGender() != null
                || f.getMinAge() != null
                || f.getMaxAge() != null
                || f.getMaritalStatus() != null
                || (f.getMinHeight() != null && !f.getMinHeight().isBlank())
                || (f.getMaxHeight() != null && !f.getMaxHeight().isBlank())
                || f.getDiet() != null
                || (f.getSurname() != null && !f.getSurname().isBlank());
    }
}
