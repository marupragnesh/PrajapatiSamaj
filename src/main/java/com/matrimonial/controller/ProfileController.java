package com.matrimonial.controller;

import com.matrimonial.dto.request.PreferenceRequest;
import com.matrimonial.dto.request.ProfileRequest;
import com.matrimonial.dto.response.ApiResponse;
import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.entity.PartnerPreference;
import com.matrimonial.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * CONTROLLER: ProfileController
 *
 * Handles all profile-related HTTP endpoints:
 *   GET    /api/profile/me               - Get own profile
 *   POST   /api/profile                  - Create profile
 *   PUT    /api/profile                  - Update profile
 *   POST   /api/profile/photos           - Upload a photo
 *   DELETE /api/profile/photos/{id}      - Delete a photo
 *   GET    /api/preferences              - Get partner preference
 *   PUT    /api/preferences              - Update partner preference
 *   GET    /api/profiles/{id}            - View another user's profile
 *
 * @AuthenticationPrincipal: Spring Security injects the logged-in user automatically.
 *
 * Layer: Controller (HTTP in/out only — no business logic)
 */
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Get the logged-in user's own profile.
     */
    @GetMapping("/api/profile/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        ProfileResponse response = profileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Create a new profile (only allowed once per user).
     */
    @PostMapping("/api/profile")
    public ResponseEntity<ProfileResponse> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileRequest request) {

        ProfileResponse response = profileService.createProfile(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update existing profile details.
     */
    @PutMapping("/api/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileRequest request) {

        ProfileResponse response = profileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload a photo to the user's profile.
     * Accepts multipart/form-data with a file field named "photo".
     * Maximum 5 photos per profile (enforced in service).
     */
    @PostMapping(value = "/api/profile/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadPhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("photo") MultipartFile file) throws IOException {

        ProfileResponse response = profileService.uploadPhoto(userDetails.getUsername(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Delete a specific photo by its ID.
     * User can only delete their own photos (enforced in service).
     */
    @DeleteMapping("/api/profile/photos/{photoId}")
    public ResponseEntity<ApiResponse> deletePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long photoId) throws IOException {

        profileService.deletePhoto(userDetails.getUsername(), photoId);
        return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully."));
    }

    /**
     * Get partner preference for the logged-in user.
     */
    @GetMapping("/api/preferences")
    public ResponseEntity<PartnerPreference> getPreference(
            @AuthenticationPrincipal UserDetails userDetails) {

        PartnerPreference preference = profileService.getPreference(userDetails.getUsername());
        return ResponseEntity.ok(preference);
    }

    /**
     * Set or update partner preference (what gender to see in discovery).
     */
    @PutMapping("/api/preferences")
    public ResponseEntity<PartnerPreference> updatePreference(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PreferenceRequest request) {

        PartnerPreference preference = profileService.updatePreference(userDetails.getUsername(), request);
        return ResponseEntity.ok(preference);
    }

    /**
     * View another user's profile by profile ID.
     * Only complete profiles are visible (enforced in service).
     */
    @GetMapping("/api/profiles/{profileId}")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable Long profileId) {
        ProfileResponse response = profileService.getProfileById(profileId);
        return ResponseEntity.ok(response);
    }
}
