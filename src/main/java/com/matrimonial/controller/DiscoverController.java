package com.matrimonial.controller;

import com.matrimonial.dto.response.ProfileResponse;
import com.matrimonial.service.DiscoverService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CONTROLLER: DiscoverController
 *
 * Handles the profile discovery / browse feature.
 *
 *   GET /api/discover?page=0&size=10 - Browse profiles matching gender preference
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
     *
     * Query params:
     *   page (default 0)  — page number (0-indexed)
     *   size (default 10) — how many profiles per page
     *
     * Example: GET /api/discover?page=0&size=10
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
}
