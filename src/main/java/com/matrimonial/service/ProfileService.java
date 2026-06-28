package com.matrimonial.service;

import com.matrimonial.dto.request.PreferenceRequest;
import com.matrimonial.dto.request.ProfileRequest;
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
 *   - Create / Update / Delete profile and account
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
    private final LikeRepository likeRepository;
    private final InterestRepository interestRepository;

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
                .isComplete(true)
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
     * Only complete profiles are visible.
     */
    public ProfileResponse getProfileById(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

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

        // First photo uploaded is automatically set as primary
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
     *
     * Business rules:
     *   - photoId is the DB primary key from profile_photos table
     *   - User can only delete their own photos (ownership check)
     *   - Physical file is deleted from disk first, then DB record is removed
     *   - If the deleted photo was primary, the next remaining photo becomes primary
     */
    @Transactional
    public void deletePhoto(String email, Long photoId) {

        User user = getUserByEmail(email);

        // Find the photo by its DB id — this is the id from PhotoDto.photoId
        ProfilePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found."));

        // Load the logged-in user's profile to verify ownership
        Profile profile = profileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        // Security check: make sure this photo belongs to the logged-in user
        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedException("You are not allowed to delete this photo.");
        }

        boolean wasPrimary = photo.getIsPrimary();

        // Step 1: Delete physical file from disk
        deleteFileFromDisk(photo.getPhotoUrl());

        // Step 2: Remove the DB record
        photoRepository.delete(photo);

        // Step 3: If deleted photo was primary, promote the next remaining photo
        if (wasPrimary) {
            List<ProfilePhoto> remaining = photoRepository.findByProfileId(profile.getId());
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsPrimary(true);
                photoRepository.save(remaining.get(0));
            }
        }
    }

    /**
     * Delete the logged-in user's account permanently.
     *
     * Deletion order (respects foreign key constraints):
     *   1. Delete all photo files from disk + DB records
     *   2. Delete all likes sent and received
     *   3. Delete all interest requests sent and received
     *   4. Delete partner preference
     *   5. Delete profile
     *   6. Delete user account
     *
     * Phase 2: Send goodbye email before deletion.
     */
    @Transactional
    public void deleteAccount(String email) {

        User user = getUserByEmail(email);

        // Step 1 — Delete photos from disk and DB
        profileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            List<ProfilePhoto> photos = photoRepository.findByProfileId(profile.getId());
            for (ProfilePhoto photo : photos) {
                deleteFileFromDisk(photo.getPhotoUrl());
            }
            photoRepository.deleteAll(photos);

            // Step 5 — Delete profile
            profileRepository.delete(profile);
        });

        // Step 2 — Delete all likes sent and received
        likeRepository.deleteBySenderId(user.getId());
        likeRepository.deleteByReceiverId(user.getId());

        // Step 3 — Delete all interest requests sent and received
        interestRepository.deleteBySenderId(user.getId());
        interestRepository.deleteByReceiverId(user.getId());

        // Step 4 — Delete partner preference
        preferenceRepository.findByUserId(user.getId())
                .ifPresent(preferenceRepository::delete);

        // Step 6 — Delete user account
        userRepository.delete(user);
    }

    /**
     * Get partner preference for the logged-in user.
     */
    public PartnerPreference getPreference(String email) {
        User user = getUserByEmail(email);
        return preferenceRepository.findByUserId(user.getId())
                .orElse(PartnerPreference.builder()
                        .user(user)
                        .preferredGender(PartnerPreference.PreferredGender.ANY)
                        .build());
    }

    /**
     * Set or update the partner preference.
     */
    @Transactional
    public PartnerPreference updatePreference(String email, PreferenceRequest request) {
        User user = getUserByEmail(email);

        PartnerPreference preference = preferenceRepository.findByUserId(user.getId())
                .orElse(PartnerPreference.builder().user(user).build());

        preference.setPreferredGender(request.getPreferredGender());
        return preferenceRepository.save(preference);
    }

    // ===== Private Helpers =====

    /**
     * Save uploaded file to local disk.
     * Returns the relative URL path to store in DB.
     *
     * Path format saved: /uploads/photos/{userId}/{uuid}.{ext}
     */
    private String saveFileLocally(MultipartFile file, Long userId) throws IOException {
        Path uploadPath = Paths.get(uploadDir + File.separator + userId);
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String uniqueFilename = UUID.randomUUID() + extension;

        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());

        return "/" + uploadDir + "/" + userId + "/" + uniqueFilename;
    }

    /**
     * Delete a photo file from local disk.
     * Silently ignores if file does not exist.
     *
     * photoUrl format: /uploads/photos/{userId}/{filename}
     * Strip the leading slash to resolve as a relative path from project root.
     */
    private void deleteFileFromDisk(String photoUrl) {
        try {
            String relativePath = photoUrl.startsWith("/") ? photoUrl.substring(1) : photoUrl;
            Path filePath = Paths.get(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log but don't fail — file may already be missing
            System.err.println("[WARN] Could not delete file from disk: " + photoUrl + " — " + e.getMessage());
        }
    }

    /**
     * Build a ProfileResponse DTO from a Profile entity.
     *
     * CHANGE: Now maps each ProfilePhoto → PhotoDto (with photoId, photoUrl, isPrimary)
     *   instead of just extracting the URL string.
     *   This allows callers to see the DB id of each photo for delete operations.
     */
    private ProfileResponse buildProfileResponse(Profile profile) {
        List<ProfilePhoto> photos = photoRepository.findByProfileId(profile.getId());

        // Map each photo entity to a PhotoDto carrying id + url + isPrimary
        List<PhotoDto> photoDtos = photos.stream()
                .map(photo -> PhotoDto.builder()
                        .photoId(photo.getId())       // DB id — use this in DELETE /api/profile/photos/{photoId}
                        .photoUrl(photo.getPhotoUrl())
                        .isPrimary(photo.getIsPrimary())
                        .build())
                .collect(Collectors.toList());

        // Extract just the primary photo URL for quick frontend access
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
                .photos(photoDtos)           // List of PhotoDto (with photoId)
                .primaryPhotoUrl(primaryPhotoUrl)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
