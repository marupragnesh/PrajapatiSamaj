package com.matrimonial.controller;

import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.dto.response.ProfileSearchResultDto;
import com.matrimonial.service.DiscoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER: DiscoverController
 *
 *   GET /api/discover?page=0&size=10       — Browse profiles by gender preference
 *   GET /api/discover/search?keyword=name  — Search profiles by full name
 *
 * Layer: Controller (HTTP in/out only — no business logic)
 */
@RestController
@RequestMapping("/api/discover")
@RequiredArgsConstructor
public class DiscoverController {

    private final DiscoverService discoverService;

    /**
     * Browse profiles filtered by the user's partner preference.
     * Query params: page (default 0), size (default 10)
     */
    @GetMapping
    public ResponseEntity<List<ProfileResponse>> discoverProfiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ProfileResponse> profiles = discoverService.discoverProfiles(
                userDetails.getUsername(), page, size);

        return ResponseEntity.ok(profiles);
    }

    /**
     * Search profiles by full name (case-insensitive, partial match).
     * Returns lightweight results: profileId + fullName + primaryPhotoUrl only.
     * Query params: keyword (required, min 1 char)
     *
     * Example: GET /api/discover/search?keyword=rahul
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProfileSearchResultDto>> searchProfiles(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<ProfileSearchResultDto> results = discoverService.searchByName(
                userDetails.getUsername(), keyword);

        return ResponseEntity.ok(results);
    }
}
