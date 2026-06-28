package com.matrimonial.service;

import com.matrimonial.dto.request.PreferenceRequest;
import com.matrimonial.dto.request.ProfileRequest;
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
 *   - Create / Update profile
 *   - Upload / Delete photos (max 5)
 *   - Get / Update partner preferences
 *   - View own profile and other profiles
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final PartnerPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    // Local folder path for storing photos (from application.properties)
    @Value("${file.upload.dir}")
    private String uploadDir;

    // Maximum number of photos allowed per profile
    private static final int MAX_PHOTOS = 5;

    /**
     * Create a new profile for the logged-in user.
     *
     * Business rules:
     *   - A user can only have ONE profile
     *   - All required fields must be provided
     *   - Profile is marked incomplete initially (is_complete = false)
     *   - Profile is marked complete when all required fields are filled
     */
    @Transactional
    public ProfileResponse createProfile(String email, ProfileRequest request) {

        User user = getUserByEmail(email);

        // Prevent creating duplicate profiles
        if (profileRepository.existsByUserId(user.getId())) {
            throw new BadRequestException("Profile already exists. Use update instead.");
        }

        Profile profile = Profile.builder()
                .user(user)
                .fullName(request.getFullName())
                .age(request.getAge())
                .gender(request.getGender())
                .city(request.getCity())
                .education(request.getEducation())
                .profession(request.getProfession())
                .religion(request.getReligion())
                .hobbies(request.getHobbies())
                .isComplete(true) // All required fields are provided on creation
                .build();

        Profile savedProfile = profileRepository.save(profile);

        return buildProfileResponse(savedProfile);
    }

    /**
     * Update an existing profile.
     *
     * Business rules:
     *   - Profile must already exist
     *   - User can only update their own profile
     */
    @Transactional
    public ProfileResponse updateProfile(String email, ProfileRequest request) {

        User user = getUserByEmail(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Please create your profile first."));

        // Update all fields
        profile.setFullName(request.getFullName());
        profile.setAge(request.getAge());
        profile.setGender(request.getGender());
        profile.setCity(request.getCity());
        profile.setEducation(request.getEducation());
        profile.setProfession(request.getProfession());
        profile.setReligion(request.getReligion());
        profile.setHobbies(request.getHobbies());
        profile.setIsComplete(true);

        Profile updatedProfile = profileRepository.save(profile);
        return buildProfileResponse(updatedProfile);
    }

    /**
     * Get the logged-in user's own profile.
     */
    public ProfileResponse getMyProfile(String email) {
        User user = getUserByEmail(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Please create your profile."));

        return buildProfileResponse(profile);
    }

    /**
     * Get another user's profile by profile ID.
     * Used when browsing or viewing profiles in discovery.
     *
     * Business rule:
     *   - Profile must be complete to be visible
     */
    public ProfileResponse getProfileById(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        // Only show complete profiles
        if (!profile.getIsComplete()) {
            throw new ResourceNotFoundException("Profile not available.");
        }

        return buildProfileResponse(profile);
    }

    /**
     * Upload a photo to the logged-in user's profile.
     *
     * Business rules:
     *   - Maximum 5 photos per profile
     *   - Only image files allowed (jpg, jpeg, png)
     *   - First photo uploaded becomes the primary photo
     *   - File saved locally (Phase 2: move to AWS S3)
     */
    @Transactional
    public ProfileResponse uploadPhoto(String email, MultipartFile file) throws IOException {

        User user = getUserByEmail(email);

        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found. Create your profile first."));

        // Check photo limit
        int currentPhotoCount = photoRepository.countByProfileId(profile.getId());
        if (currentPhotoCount >= MAX_PHOTOS) {
            throw new BadRequestException("You can upload a maximum of " + MAX_PHOTOS + " photos.");
        }

        // Validate file type (only images allowed)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed (jpg, jpeg, png).");
        }

        // Save file to local storage
        String photoUrl = saveFileLocally(file, user.getId());

        // First photo = primary photo automatically
        boolean isPrimary = (currentPhotoCount == 0);

        ProfilePhoto photo = ProfilePhoto.builder()
                .profile(profile)
                .photoUrl(photoUrl)
                .isPrimary(isPrimary)
                .build();

        photoRepository.save(photo);

        // Return updated profile with new photo list
        return buildProfileResponse(profileRepository.findById(profile.getId()).get());
    }

    /**
     * Delete a photo from the logged-in user's profile.
     *
     * Business rules:
     *   - User can only delete their own photos
     *   - If primary photo is deleted, first remaining photo becomes primary
     */
    @Transactional
    public void deletePhoto(String email, Long photoId) {

        User user = getUserByEmail(email);

        ProfilePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found."));

        // Ensure the photo belongs to this user's profile
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedException("You are not allowed to delete this photo.");
        }

        boolean wasPrimary = photo.getIsPrimary();
        photoRepository.delete(photo);

        // If deleted photo was primary, set the next photo as primary
        if (wasPrimary) {
            List<ProfilePhoto> remaining = photoRepository.findByProfileId(profile.getId());
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsPrimary(true);
                photoRepository.save(remaining.get(0));
            }
        }
    }

    /**
     * Get the partner preference for the logged-in user.
     */
    public PartnerPreference getPreference(String email) {
        User user = getUserByEmail(email);
        // Return existing preference, or create a default one (ANY gender)
        return preferenceRepository.findByUserId(user.getId())
                .orElse(PartnerPreference.builder()
                        .user(user)
                        .preferredGender(PartnerPreference.PreferredGender.ANY)
                        .build());
    }

    /**
     * Set or update the partner preference.
     *
     * Business rule:
     *   - Only one preference per user (upsert behavior)
     */
    @Transactional
    public PartnerPreference updatePreference(String email, PreferenceRequest request) {
        User user = getUserByEmail(email);

        // Find existing preference or create a new one
        PartnerPreference preference = preferenceRepository.findByUserId(user.getId())
                .orElse(PartnerPreference.builder().user(user).build());

        preference.setPreferredGender(request.getPreferredGender());
        return preferenceRepository.save(preference);
    }

    // ===== Private Helpers =====

    /**
     * Save uploaded file to local disk.
     * Returns the relative URL/path to store in DB.
     */
    private String saveFileLocally(MultipartFile file, Long userId) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir + File.separator + userId);
        Files.createDirectories(uploadPath);

        // Generate unique filename to prevent conflicts
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String uniqueFilename = UUID.randomUUID() + extension;

        // Write file to disk
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());

        // Return URL path (served as static resource)
        return "/" + uploadDir + "/" + userId + "/" + uniqueFilename;
    }

    /**
     * Build a ProfileResponse DTO from a Profile entity.
     * Extracts photo URLs and primary photo URL.
     */
    private ProfileResponse buildProfileResponse(Profile profile) {
        List<ProfilePhoto> photos = photoRepository.findByProfileId(profile.getId());

        // Collect all photo URLs
        List<String> photoUrls = photos.stream()
                .map(ProfilePhoto::getPhotoUrl)
                .collect(Collectors.toList());

        // Find primary photo URL
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

    // Load user entity from email — shared helper
    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
