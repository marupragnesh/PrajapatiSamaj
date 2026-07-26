package com.matrimonial.mapper;

import com.matrimonial.dto.response.ExpectationResponse;
import com.matrimonial.dto.response.PhotoDto;
import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.dto.response.ProfileSearchResultDto;
import com.matrimonial.entity.Expectation;
import com.matrimonial.entity.Profile;
import com.matrimonial.entity.ProfilePhoto;
import com.matrimonial.entity.User;
import com.matrimonial.entity.enums.PaymentFeature;
import com.matrimonial.repository.ExpectationRepository;
import com.matrimonial.repository.PhotoRepository;
import com.matrimonial.service.PaymentService;
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
    private final PaymentService paymentService;

    /**
     * Build the full ProfileResponse for a profile, WITHOUT a viewer context.
     * Used only by ProfileService.getMyProfile where the viewer is always the
     * owner — mobile number is shown in full, no payment check needed.
     *
     * @param profile        the Profile entity to convert
     * @param isOwnProfile   true if the viewer IS this profile's owner —
     *                       controls whether mobileNo is shown in full or masked
     */
    public ProfileResponse toProfileResponse(Profile profile, boolean isOwnProfile) {
        return toProfileResponse(profile, isOwnProfile, null);
    }

    /**
     * Build the full ProfileResponse for a profile, WITH viewer context.
     * Used by ProfileService.getProfileById and DiscoverService.discoverProfiles,
     * where the viewer may not be the owner but may have paid to unlock contact info.
     *
     * @param profile        the Profile entity to convert
     * @param isOwnProfile   true if the viewer IS this profile's owner
     * @param viewer         the logged-in user viewing this profile — pass null
     *                       only when isOwnProfile is true and no unlock check is needed
     */
    public ProfileResponse toProfileResponse(Profile profile, boolean isOwnProfile, User viewer) {
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
                .name(profile.getName())
                .surname(profile.getSurname())
                .fullName(profile.getFullName())
                .age(profile.getAge())
                .gender(profile.getGender())
                .maritalStatus(profile.getMaritalStatus())
                .city(profile.getCity())
                .mobileNo(maskMobileIfNeeded(profile.getMobileNo(), isOwnProfile, viewer))
                .isMobileUnlocked(isMobileUnlocked(isOwnProfile, viewer))
                .addressLine(profile.getAddressLine())
                .state(profile.getState())
                .pincode(profile.getPincode())
                .education(profile.getEducation())
                .profession(profile.getProfession())
                .dateOfBirth(profile.getDateOfBirth())
                .birthTime(profile.getBirthTime())
                .weight(profile.getWeight())
                .bloodGroup(profile.getBloodGroup())
                .birthPlace(profile.getBirthPlace())
                .hasMangal(profile.getHasMangal())
                .hasSani(profile.getHasSani())
                .alternateMobileNo(profile.getAlternateMobileNo())
                .height(profile.getHeight())
                .income(profile.getIncome())
                .gotra(profile.getGotra())
                .diet(profile.getDiet())
                .religion(profile.getReligion())
                .hobbies(profile.getHobbies())
                .fatherName(profile.getFatherName())
                .fatherOccupation(profile.getFatherOccupation())
                .motherName(profile.getMotherName())
                .motherOccupation(profile.getMotherOccupation())
                .description(profile.getDescription())
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
                .preferredState(expectation.getPreferredState())
                .preferredHasMangal(expectation.getPreferredHasMangal())
                .preferredHasSani(expectation.getPreferredHasSani())
                .preferredMinWeight(expectation.getPreferredMinWeight())
                .preferredMaxWeight(expectation.getPreferredMaxWeight())
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
     * owner, OR the viewer has paid to unlock CONTACT_UNLOCK (account-wide —
     * one payment unlocks every profile's number, not just this one).
     * Returns null unchanged (e.g. for legacy rows with no number yet).
     */
    private String maskMobileIfNeeded(String mobileNo, boolean isOwnProfile, User viewer) {
        if (mobileNo == null || mobileNo.length() < 2) {
            return mobileNo;
        }
        if (isMobileUnlocked(isOwnProfile, viewer)) {
            return mobileNo;
        }
        return mobileNo.substring(0, 2) + "*".repeat(mobileNo.length() - 2);
    }

    /**
     * Single source of truth for "does this viewer see the real mobile number?"
     * True if the viewer owns the profile, OR has paid to unlock CONTACT_UNLOCK.
     * Used by BOTH maskMobileIfNeeded (to decide what string to show) and the
     * isMobileUnlocked response field (so the frontend can show an Unlock
     * button) — kept as one method so the two can never disagree with each other.
     */
    private boolean isMobileUnlocked(boolean isOwnProfile, User viewer) {
        if (isOwnProfile) {
            return true;
        }
        return viewer != null && paymentService.hasUnlocked(viewer, PaymentFeature.CONTACT_UNLOCK);
    }
}
