package com.matrimonial.service;

import com.matrimonial.dto.request.ExpectationRequest;
import com.matrimonial.dto.request.PreferenceRequest;
import com.matrimonial.dto.request.ProfileRequest;
import com.matrimonial.dto.response.ExpectationResponse;
import com.matrimonial.dto.response.PhotoDto;
import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.entity.*;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.exception.UnauthorizedException;
import com.matrimonial.repository.*;
import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

/**
 * SERVICE: ProfileService
 *
 * Handles all profile-related business logic:
 *   - Create / Update profile (including new fields: maritalStatus, height, income, gotra, diet)
 *   - Upload / Delete / Set Primary photo
 *   - Get / Update partner preferences
 *   - Get / Save expectations (all fields including new ones)
 *   - View own profile and other profiles
 *   - Delete account
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final PartnerPreferenceRepository preferenceRepository;
    private final ExpectationRepository expectationRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final InterestRepository interestRepository;

    @Value("${file.upload.dir}")
    private String uploadDir;

    private static final int MAX_PHOTOS = 5;

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

        return buildProfileResponse(profileRepository.save(profile));
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
        profile.setEducation(request.getEducation());
        profile.setProfession(request.getProfession());
        profile.setHeight(request.getHeight());
        profile.setIncome(request.getIncome());
        profile.setGotra(request.getGotra());
        profile.setDiet(request.getDiet());
        profile.setReligion(request.getReligion());
        profile.setHobbies(request.getHobbies());
        profile.setIsComplete(true);

        return buildProfileResponse(profileRepository.save(profile));
    }

    /** Get the logged-in user's own profile. */
    public ProfileResponse getMyProfile(String email) {
        User user = getUserByEmail(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Please create your profile."));

        return buildProfileResponse(profile);
    }

    /** Get another user's profile by profile ID (only complete profiles). */
    public ProfileResponse getProfileById(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        if (!profile.getIsComplete()) {
            throw new ResourceNotFoundException("Profile not available.");
        }

        return buildProfileResponse(profile);
    }

    // ===== Photos =====

    /**
     * Upload a photo to the logged-in user's profile.
     * First photo is automatically set as primary.
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

        photoRepository.save(photo);
        return buildProfileResponse(profileRepository.findById(profile.getId()).get());
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

        return buildProfileResponse(profileRepository.findById(profile.getId()).get());
    }

    // ===== Expectations =====

    /**
     * Get expectations for the logged-in user.
     * Returns empty response (all nulls) if not yet filled.
     */
    public ExpectationResponse getMyExpectations(String email) {
        User user = getUserByEmail(email);
        return expectationRepository.findByUserId(user.getId())
                .map(this::buildExpectationResponse)
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

        return buildExpectationResponse(expectationRepository.save(expectation));
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
            System.err.println("[WARN] Could not delete file from disk: " + photoUrl + " — " + e.getMessage());
        }
    }

    /** Build ProfileResponse from Profile entity including photos and expectations. */
    private ProfileResponse buildProfileResponse(Profile profile) {
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

        // Load expectations — null if user has not filled them in
        ExpectationResponse expectations = expectationRepository
                .findByUserId(profile.getUser().getId())
                .map(this::buildExpectationResponse)
                .orElse(null);

        return ProfileResponse.builder()
                .profileId(profile.getId())
                .userId(profile.getUser().getId())
                .fullName(profile.getFullName())
                .age(profile.getAge())
                .gender(profile.getGender())
                .maritalStatus(profile.getMaritalStatus())
                .city(profile.getCity())
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
    private ExpectationResponse buildExpectationResponse(Expectation expectation) {
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

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
