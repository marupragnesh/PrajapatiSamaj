package com.matrimonial.mapper;

import com.matrimonial.dto.response.ExpectationResponse;
import com.matrimonial.dto.response.PhotoDto;
import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.dto.response.ProfileSearchResultDto;
import com.matrimonial.entity.Expectation;
import com.matrimonial.entity.Profile;
import com.matrimonial.entity.ProfilePhoto;
import com.matrimonial.repository.ExpectationRepository;
import com.matrimonial.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MAPPER: ProfileMapper
 *
 * Single source of truth for converting Profile/Expectation entities into
 * response DTOs. Both ProfileService and DiscoverService delegate here so
 * that every new field only needs to be added in ONE place.
 *
 * Why this exists: previously ProfileService and DiscoverService each had
 * their own private buildProfileResponse() — they drifted out of sync
 * (DiscoverService was missing maritalStatus, height, etc.). This mapper
 * fixes that root cause.
 *
 * Layer: sits alongside Service layer — pure entity→DTO conversion, no
 * persistence calls except read-only lookups needed to assemble the DTO.
 */
@Component
@RequiredArgsConstructor
public class ProfileMapper {

    private final PhotoRepository photoRepository;
    private final ExpectationRepository expectationRepository;

    /**
     * Build the full ProfileResponse for a profile.
     *
     * @param profile        the Profile entity to convert
     * @param isOwnProfile   true if the viewer IS this profile's owner —
     *                       controls whether mobileNo is shown in full or masked
     */
    public ProfileResponse toProfileResponse(Profile profile, boolean isOwnProfile) {
        List<ProfilePhoto> photos = photoRepository.findByProfileId(profile.getId());

        List<PhotoDto> photoDtos = photos.stream()
                .map(photo -> PhotoDto.builder()
                        .photoId(photo.getId())
                        .photoUrl(photo.getPhotoUrl())
                        .isPrimary(photo.getIsPrimary())
                        .build())
                .collect(Collectors.toList());

        String primaryPhotoUrl = photos.stream()
                .filter(ProfilePhoto::getIsPrimary)
                .map(ProfilePhoto::getPhotoUrl)
                .findFirst()
                .orElse(null);

        ExpectationResponse expectations = expectationRepository
                .findByUserId(profile.getUser().getId())
                .map(this::toExpectationResponse)
                .orElse(null);

        return ProfileResponse.builder()
                .profileId(profile.getId())
                .userId(profile.getUser().getId())
                .fullName(profile.getFullName())
                .age(profile.getAge())
                .gender(profile.getGender())
                .maritalStatus(profile.getMaritalStatus())
                .city(profile.getCity())
                .mobileNo(maskMobileIfNeeded(profile.getMobileNo(), isOwnProfile))
                .addressLine(profile.getAddressLine())
                .state(profile.getState())
                .pincode(profile.getPincode())
                .education(profile.getEducation())
                .profession(profile.getProfession())
                .height(profile.getHeight())
                .income(profile.getIncome())
                .gotra(profile.getGotra())
                .diet(profile.getDiet())
                .religion(profile.getReligion())
                .hobbies(profile.getHobbies())
                .isComplete(profile.getIsComplete())
                .photos(photoDtos)
                .primaryPhotoUrl(primaryPhotoUrl)
                .expectations(expectations)
                .build();
    }

    /** Build ExpectationResponse from Expectation entity. */
    public ExpectationResponse toExpectationResponse(Expectation expectation) {
        return ExpectationResponse.builder()
                .minAge(expectation.getMinAge())
                .maxAge(expectation.getMaxAge())
                .preferredMaritalStatus(expectation.getPreferredMaritalStatus())
                .preferredMinHeight(expectation.getPreferredMinHeight())
                .preferredMaxHeight(expectation.getPreferredMaxHeight())
                .preferredCity(expectation.getPreferredCity())
                .preferredEducation(expectation.getPreferredEducation())
                .preferredProfession(expectation.getPreferredProfession())
                .preferredIncome(expectation.getPreferredIncome())
                .preferredGotra(expectation.getPreferredGotra())
                .preferredDiet(expectation.getPreferredDiet())
                .preferredReligion(expectation.getPreferredReligion())
                .aboutExpectations(expectation.getAboutExpectations())
                .build();
    }

    /**
     * Build the lightweight search result DTO (name + DP only).
     * Used by the "search by name" feature — deliberately excludes every
     * other field to keep search responses small and fast.
     */
    public ProfileSearchResultDto toSearchResultDto(Profile profile) {
        String primaryPhotoUrl = photoRepository.findByProfileId(profile.getId()).stream()
                .filter(ProfilePhoto::getIsPrimary)
                .map(ProfilePhoto::getPhotoUrl)
                .findFirst()
                .orElse(null);

        return ProfileSearchResultDto.builder()
                .profileId(profile.getId())
                .fullName(profile.getFullName())
                .primaryPhotoUrl(primaryPhotoUrl)
                .build();
    }

    /**
     * Mask a mobile number as "98********" unless the viewer is the profile
     * owner. Returns null unchanged (e.g. for legacy rows with no number yet).
     */
    private String maskMobileIfNeeded(String mobileNo, boolean isOwnProfile) {
        if (mobileNo == null || mobileNo.length() < 2) {
            return mobileNo;
        }
        if (isOwnProfile) {
            return mobileNo;
        }
        return mobileNo.substring(0, 2) + "*".repeat(mobileNo.length() - 2);
    }
}
