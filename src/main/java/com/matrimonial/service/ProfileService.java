package com.matrimonial.service;

import com.matrimonial.dto.request.ExpectationRequest;
import com.matrimonial.dto.request.PreferenceRequest;
import com.matrimonial.dto.request.ProfileRequest;
import com.matrimonial.dto.response.ExpectationResponse;
import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.entity.*;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.exception.UnauthorizedException;
import com.matrimonial.mapper.ProfileMapper;
import com.matrimonial.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * SERVICE: ProfileService
 *
 * Handles all profile-related business logic:
 *   - Create / Update profile (including address, mobileNo, maritalStatus, height, income, gotra, diet)
 *   - Upload / Delete / Set Primary photo (max 10 per profile)
 *   - Get / Update partner preferences
 *   - Get / Save expectations
 *   - View own profile (full mobile number) and other profiles (masked mobile number)
 *   - Delete account
 *
 * Entity → DTO conversion is delegated to ProfileMapper so that
 * ProfileService and DiscoverService never drift out of sync again.
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final PartnerPreferenceRepository preferenceRepository;
    private final ExpectationRepository expectationRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final InterestRepository interestRepository;
    private final ProfileMapper profileMapper;

    @Value("${file.upload.dir}")
    private String uploadDir;

    private static final int MAX_PHOTOS = 10;

    // ===== Profile CRUD =====

    /** Create a new profile for the logged-in user (only once per user). */
    @Transactional
    public ProfileResponse createProfile(String email, ProfileRequest request) {
        User user = getUserByEmail(email);

        if (profileRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("Profile already exists. Use update instead.");
        }

        Profile profile = Profile.builder()
                .user(user)
                .fullName(request.getFullName())
                .age(request.getAge())
                .gender(request.getGender())
                .maritalStatus(request.getMaritalStatus())
                .city(request.getCity())
                .mobileNo(request.getMobileNo())
                .addressLine(request.getAddressLine())
                .state(request.getState())
                .pincode(request.getPincode())
                .education(request.getEducation())
                .profession(request.getProfession())
                .height(request.getHeight())
                .income(request.getIncome())
                .gotra(request.getGotra())
                .diet(request.getDiet())
                .religion(request.getReligion())
                .hobbies(request.getHobbies())
                .isComplete(true)
                .build();

        Profile saved = profileRepository.save(profile);
        log.info("Profile created — userId={}", saved.getUser().getId());
        return profileMapper.toProfileResponse(saved, true);
    }

    /** Update an existing profile. */
    @Transactional
    public ProfileResponse updateProfile(String email, ProfileRequest request) {
        User user = getUserByEmail(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Please create your profile first."));

        profile.setFullName(request.getFullName());
        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setMaritalStatus(request.getMaritalStatus());
        profile.setCity(request.getCity());
        profile.setMobileNo(request.getMobileNo());
        profile.setAddressLine(request.getAddressLine());
        profile.setState(request.getState());
        profile.setPincode(request.getPincode());
        profile.setEducation(request.getEducation());
        profile.setProfession(request.getProfession());
        profile.setHeight(request.getHeight());
        profile.setIncome(request.getIncome());
        profile.setGotra(request.getGotra());
        profile.setDiet(request.getDiet());
        profile.setReligion(request.getReligion());
        profile.setHobbies(request.getHobbies());
        profile.setIsComplete(true);

        Profile saved = profileRepository.save(profile);
        log.info("Profile updated — userId={}", saved.getUser().getId());
        return profileMapper.toProfileResponse(saved, true);
    }

    /** Get the logged-in user's own profile — full (unmasked) mobile number. */
    public ProfileResponse getMyProfile(String email) {
        User user = getUserByEmail(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Please create your profile."));

        return profileMapper.toProfileResponse(profile, true);
    }

    /** Get another user's profile by profile ID (only complete profiles) — mobile number masked. */
    public ProfileResponse getProfileById(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        if (!profile.getIsComplete()) {
            throw new ResourceNotFoundException("Profile not available.");
        }

        return profileMapper.toProfileResponse(profile, false);
    }

    // ===== Photos =====

    /**
     * Upload a photo to the logged-in user's profile.
     * First photo is automatically set as primary. Max 10 photos per profile.
     */
    @Transactional
    public ProfileResponse uploadPhoto(String email, MultipartFile file) throws IOException {
        User user = getUserByEmail(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Create your profile first."));

        int currentPhotoCount = photoRepository.countByProfileId(profile.getId());
        if (currentPhotoCount >= MAX_PHOTOS) {
            throw new BadRequestException("You can upload a maximum of " + MAX_PHOTOS + " photos.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed (jpg, jpeg, png).");
        }

        String photoUrl = saveFileLocally(file, user.getId());

        // First photo uploaded is automatically primary
        boolean isPrimary = (currentPhotoCount == 0);

        ProfilePhoto photo = ProfilePhoto.builder()
                .profile(profile)
                .photoUrl(photoUrl)
                .isPrimary(isPrimary)
                .build();

        ProfilePhoto savedPhoto = photoRepository.save(photo);
        log.info("Photo uploaded — userId={}, photoId={}", user.getId(), savedPhoto.getId());
        Profile reloaded = profileRepository.findById(profile.getId()).get();
        return profileMapper.toProfileResponse(reloaded, true);
    }

    /**
     * Delete a specific photo from the logged-in user's profile.
     * If deleted photo was primary → next remaining photo becomes primary.
     */
    @Transactional
    public void deletePhoto(String email, Long photoId) {
        User user = getUserByEmail(email);

        ProfilePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found."));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedException("You are not allowed to delete this photo.");
        }

        boolean wasPrimary = photo.getIsPrimary();

        deleteFileFromDisk(photo.getPhotoUrl());
        photoRepository.delete(photo);
        log.info("Photo deleted — userId={}, photoId={}", user.getId(), photoId);

        // Promote next photo to primary if deleted one was primary
        if (wasPrimary) {
            List<ProfilePhoto> remaining = photoRepository.findByProfileId(profile.getId());
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsPrimary(true);
                photoRepository.save(remaining.get(0));
            }
        }
    }

    /**
     * Set a specific photo as the primary (profile picture).
     * Clears isPrimary on all other photos for this profile first.
     */
    @Transactional
    public ProfileResponse setPrimaryPhoto(String email, Long photoId) {
        User user = getUserByEmail(email);

        ProfilePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found."));

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedException("You are not allowed to modify this photo.");
        }

        // Unset primary on all photos for this profile
        List<ProfilePhoto> allPhotos = photoRepository.findByProfileId(profile.getId());
        for (ProfilePhoto p : allPhotos) {
            if (p.getIsPrimary()) {
                p.setIsPrimary(false);
                photoRepository.save(p);
            }
        }

        photo.setIsPrimary(true);
        photoRepository.save(photo);

        Profile reloaded = profileRepository.findById(profile.getId()).get();
        return profileMapper.toProfileResponse(reloaded, true);
    }

    // ===== Expectations =====

    /**
     * Get expectations for the logged-in user.
     * Returns empty response (all nulls) if not yet filled.
     */
    public ExpectationResponse getMyExpectations(String email) {
        User user = getUserByEmail(email);
        return expectationRepository.findByUserId(user.getId())
                .map(profileMapper::toExpectationResponse)
                .orElse(new ExpectationResponse());
    }

    /**
     * Save or update partner expectations.
     * Upsert: creates if not exists, updates if exists.
     */
    @Transactional
    public ExpectationResponse saveExpectations(String email, ExpectationRequest request) {
        User user = getUserByEmail(email);

        Expectation expectation = expectationRepository.findByUserId(user.getId())
                .orElse(Expectation.builder().user(user).build());

        expectation.setMinAge(request.getMinAge());
        expectation.setMaxAge(request.getMaxAge());
        expectation.setPreferredMaritalStatus(request.getPreferredMaritalStatus());
        expectation.setPreferredMinHeight(request.getPreferredMinHeight());
        expectation.setPreferredMaxHeight(request.getPreferredMaxHeight());
        expectation.setPreferredCity(request.getPreferredCity());
        expectation.setPreferredEducation(request.getPreferredEducation());
        expectation.setPreferredProfession(request.getPreferredProfession());
        expectation.setPreferredIncome(request.getPreferredIncome());
        expectation.setPreferredGotra(request.getPreferredGotra());
        expectation.setPreferredDiet(request.getPreferredDiet());
        expectation.setPreferredReligion(request.getPreferredReligion());
        expectation.setAboutExpectations(request.getAboutExpectations());

        Expectation saved = expectationRepository.save(expectation);
        log.info("Expectations saved — userId={}", user.getId());
        return profileMapper.toExpectationResponse(saved);
    }

    // ===== Preferences =====

    public PartnerPreference getPreference(String email) {
        User user = getUserByEmail(email);
        return preferenceRepository.findByUserId(user.getId())
                .orElse(PartnerPreference.builder()
                        .user(user)
                        .preferredGender(PartnerPreference.PreferredGender.ANY)
                        .build());
    }

    @Transactional
    public PartnerPreference updatePreference(String email, PreferenceRequest request) {
        User user = getUserByEmail(email);

        PartnerPreference preference = preferenceRepository.findByUserId(user.getId())
                .orElse(PartnerPreference.builder().user(user).build());

        preference.setPreferredGender(request.getPreferredGender());
        return preferenceRepository.save(preference);
    }

    // ===== Account Deletion =====

    /**
     * Permanently delete the logged-in user's account.
     * Deletion order: photos → likes → interests → preference → expectations → profile → user
     */
    @Transactional
    public void deleteAccount(String email) {
        User user = getUserByEmail(email);

        profileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            List<ProfilePhoto> photos = photoRepository.findByProfileId(profile.getId());
            for (ProfilePhoto photo : photos) {
                deleteFileFromDisk(photo.getPhotoUrl());
            }
            photoRepository.deleteAll(photos);
            profileRepository.delete(profile);
        });

        likeRepository.deleteBySenderId(user.getId());
        likeRepository.deleteByReceiverId(user.getId());
        interestRepository.deleteBySenderId(user.getId());
        interestRepository.deleteByReceiverId(user.getId());
        preferenceRepository.findByUserId(user.getId()).ifPresent(preferenceRepository::delete);
        expectationRepository.findByUserId(user.getId()).ifPresent(expectationRepository::delete);

        userRepository.delete(user);
        log.info("Account deleted — userId={}", user.getId());
    }

    // ===== Private Helpers =====

    /** Save uploaded file to local disk, return relative URL path. */
    private String saveFileLocally(MultipartFile file, Long userId) throws IOException {
        Path uploadPath = Paths.get(uploadDir + File.separator + userId);
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String uniqueFilename = UUID.randomUUID() + extension;

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());

        return "/" + uploadDir + "/" + userId + "/" + uniqueFilename;
    }

    /** Delete a photo file from local disk — silently ignores if missing. */
    private void deleteFileFromDisk(String photoUrl) {
        try {
            String relativePath = photoUrl.startsWith("/") ? photoUrl.substring(1) : photoUrl;
            Files.deleteIfExists(Paths.get(relativePath));
        } catch (IOException e) {
            log.warn("Could not delete file from disk: {} — {}", photoUrl, e.getMessage());
        }
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
