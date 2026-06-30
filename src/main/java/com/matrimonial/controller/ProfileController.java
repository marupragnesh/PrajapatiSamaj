package com.matrimonial.controller;

import com.matrimonial.dto.request.ExpectationRequest;
import com.matrimonial.dto.request.PreferenceRequest;
import com.matrimonial.dto.request.ProfileRequest;
import com.matrimonial.dto.response.ApiResponse;
import com.matrimonial.dto.response.ExpectationResponse;
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
 *   GET    /api/profile/me                      - Get own profile
 *   POST   /api/profile                         - Create profile
 *   PUT    /api/profile                         - Update profile
 *   POST   /api/profile/photos                  - Upload a photo
 *   DELETE /api/profile/photos/{id}             - Delete a photo
 *   PUT    /api/profile/photos/{id}/primary     - Set photo as primary
 *   GET    /api/profile/expectations            - Get partner expectations
 *   PUT    /api/profile/expectations            - Save partner expectations
 *   GET    /api/preferences                     - Get partner preference
 *   PUT    /api/preferences                     - Update partner preference
 *   GET    /api/profiles/{id}                   - View another user's profile
 *   DELETE /api/account                         - Permanently delete account
 *
 * Layer: Controller (HTTP in/out only — no business logic)
 */
@RestController
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    // ===== Profile =====

    /** Get the logged-in user's own profile. */
    @GetMapping("/api/profile/me")
    public ResponseEntity<ProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        ProfileResponse response = profileService.getMyProfile(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /** Create a new profile — only allowed once per user. */
    @PostMapping("/api/profile")
    public ResponseEntity<ProfileResponse> createProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileRequest request) {

        ProfileResponse response = profileService.createProfile(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Update existing profile details. */
    @PutMapping("/api/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileRequest request) {

        ProfileResponse response = profileService.updateProfile(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    // ===== Photos =====

    /**
     * Upload a photo — multipart/form-data, field name "photo".
     * Max 5 photos per profile (enforced in service).
     */
    @PostMapping(value = "/api/profile/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> uploadPhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("photo") MultipartFile file) throws IOException {

        ProfileResponse response = profileService.uploadPhoto(userDetails.getUsername(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Delete a specific photo by its numeric ID. User can only delete their own photos. */
    @DeleteMapping("/api/profile/photos/{photoId}")
    public ResponseEntity<ApiResponse> deletePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long photoId) {

        profileService.deletePhoto(userDetails.getUsername(), photoId);
        return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully."));
    }

    /**
     * Set a specific photo as the primary (profile picture).
     * Clears isPrimary on all other photos for this profile.
     */
    @PutMapping("/api/profile/photos/{photoId}/primary")
    public ResponseEntity<ProfileResponse> setPrimaryPhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long photoId) {

        ProfileResponse response = profileService.setPrimaryPhoto(userDetails.getUsername(), photoId);
        return ResponseEntity.ok(response);
    }

    // ===== Expectations =====

    /**
     * Get the logged-in user's partner expectations.
     * Returns empty object (all nulls) if not yet filled in.
     */
    @GetMapping("/api/profile/expectations")
    public ResponseEntity<ExpectationResponse> getMyExpectations(
            @AuthenticationPrincipal UserDetails userDetails) {

        ExpectationResponse response = profileService.getMyExpectations(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    /**
     * Save or update partner expectations — upsert (creates if not exists).
     * All fields are optional — user can fill any combination.
     */
    @PutMapping("/api/profile/expectations")
    public ResponseEntity<ExpectationResponse> saveExpectations(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ExpectationRequest request) {

        ExpectationResponse response = profileService.saveExpectations(userDetails.getUsername(), request);
        return ResponseEntity.ok(response);
    }

    // ===== Partner Preference =====

    /** Get partner preference (which gender to show in discover page). */
    @GetMapping("/api/preferences")
    public ResponseEntity<PartnerPreference> getPreference(
            @AuthenticationPrincipal UserDetails userDetails) {

        PartnerPreference preference = profileService.getPreference(userDetails.getUsername());
        return ResponseEntity.ok(preference);
    }

    /** Set or update partner preference. */
    @PutMapping("/api/preferences")
    public ResponseEntity<PartnerPreference> updatePreference(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PreferenceRequest request) {

        PartnerPreference preference = profileService.updatePreference(userDetails.getUsername(), request);
        return ResponseEntity.ok(preference);
    }

    // ===== Other Users =====

    /** View another user's profile by profile ID. Only complete profiles are visible. */
    @GetMapping("/api/profiles/{profileId}")
    public ResponseEntity<ProfileResponse> getProfileById(@PathVariable Long profileId) {
        ProfileResponse response = profileService.getProfileById(profileId);
        return ResponseEntity.ok(response);
    }

    // ===== Account =====

    /**
     * Permanently delete the logged-in user's account.
     * Deletion order: photos (disk + DB) → likes → interests → preference → expectations → profile → user.
     * This action is IRREVERSIBLE.
     */
    @DeleteMapping("/api/account")
    public ResponseEntity<ApiResponse> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {

        profileService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Your account has been permanently deleted."));
    }
}
