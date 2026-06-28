package com.matrimonial.service;

import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.entity.*;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.repository.*;
import lombok.RequiredArgsConstructor;
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
 * Handles the discovery / browse profiles feature.
 *
 * Business rules:
 *   - Only show profiles where is_complete = true
 *   - Filter by user's gender preference (MALE, FEMALE, or ANY)
 *   - Exclude the logged-in user's own profile
 *   - Sort by recently joined (created_at DESC)
 *   - Pagination support (default 10 per page)
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
public class DiscoverService {

    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final PartnerPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    /**
     * Get a paginated list of profiles matching the logged-in user's preference.
     *
     * @param email  logged-in user's email
     * @param page   page number (0-indexed)
     * @param size   number of profiles per page
     * @return list of ProfileResponse DTOs
     */
    public List<ProfileResponse> discoverProfiles(String email, int page, int size) {

        // Load the current user
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // Get the user's partner preference (default: ANY)
        PartnerPreference preference = preferenceRepository.findByUserId(currentUser.getId())
                .orElse(PartnerPreference.builder()
                        .preferredGender(PartnerPreference.PreferredGender.ANY)
                        .build());

        // Sort by most recently created profile first
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Profile> profilePage;

        // Filter by gender preference
        if (preference.getPreferredGender() == PartnerPreference.PreferredGender.ANY) {
            // Show all complete profiles (excluding current user)
            profilePage = profileRepository.findByUserIdNotAndIsCompleteTrue(
                    currentUser.getId(), pageable);
        } else {
            // Convert PreferredGender enum to Profile.Gender enum for the query
            Profile.Gender genderFilter = Profile.Gender.valueOf(preference.getPreferredGender().name());

            // Show only profiles of the preferred gender (excluding current user)
            profilePage = profileRepository.findByGenderAndUserIdNotAndIsCompleteTrue(
                    genderFilter, currentUser.getId(), pageable);
        }

        // Convert Profile entities to ProfileResponse DTOs
        return profilePage.getContent().stream()
                .map(this::buildProfileResponse)
                .collect(Collectors.toList());
    }

    // ===== Private Helper =====

    /**
     * Build ProfileResponse DTO from a Profile entity.
     * Loads photos for each profile.
     */
    private ProfileResponse buildProfileResponse(Profile profile) {
        List<ProfilePhoto> photos = photoRepository.findByProfileId(profile.getId());

        List<String> photoUrls = photos.stream()
                .map(ProfilePhoto::getPhotoUrl)
                .collect(Collectors.toList());

        String primaryPhotoUrl = photos.stream()
                .filter(ProfilePhoto::getIsPrimary)
                .map(ProfilePhoto::getPhotoUrl)
                .findFirst()
                .orElse(null);

        return ProfileResponse.builder()
                .profileId(profile.getId())
                .userId(profile.getUser().getId())
                .fullName(profile.getFullName())
                .age(profile.getAge())
                .gender(profile.getGender())
                .city(profile.getCity())
                .education(profile.getEducation())
                .profession(profile.getProfession())
                .religion(profile.getReligion())
                .hobbies(profile.getHobbies())
                .isComplete(profile.getIsComplete())
                .photoUrls(photoUrls)
                .primaryPhotoUrl(primaryPhotoUrl)
                .build();
    }
}
