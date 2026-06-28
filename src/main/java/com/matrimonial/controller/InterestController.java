package com.matrimonial.controller;

import com.matrimonial.dto.response.ApiResponse;
import com.matrimonial.dto.response.InterestResponse;
import com.matrimonial.dto.response.MatchResponse;
import com.matrimonial.service.InterestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER: InterestController
 *
 * Handles all Interest Request HTTP endpoints:
 *   POST /api/interests/{profileId}      - Send interest request
 *   GET  /api/interests/received         - View received (PENDING) interests
 *   PUT  /api/interests/{id}/accept      - Accept a received interest
 *   PUT  /api/interests/{id}/decline     - Decline a received interest
 *   GET  /api/interests/matches          - View mutual matches
 *
 * Layer: Controller (HTTP in/out only — no business logic)
 */
@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    /**
     * Send an interest request to another user's profile.
     */
    @PostMapping("/{profileId}")
    public ResponseEntity<ApiResponse> sendInterest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long profileId) {

        interestService.sendInterest(userDetails.getUsername(), profileId);
        return ResponseEntity.ok(ApiResponse.success("Interest request sent successfully."));
    }

    /**
     * Get all PENDING interest requests received by the logged-in user.
     * Returns InterestResponse DTO — safe from lazy loading issues.
     */
    @GetMapping("/received")
    public ResponseEntity<List<InterestResponse>> getReceivedInterests(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<InterestResponse> interests = interestService.getReceivedInterests(userDetails.getUsername());
        return ResponseEntity.ok(interests);
    }

    /**
     * Accept a received interest request.
     */
    @PutMapping("/{interestId}/accept")
    public ResponseEntity<ApiResponse> acceptInterest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long interestId) {

        interestService.acceptInterest(userDetails.getUsername(), interestId);
        return ResponseEntity.ok(ApiResponse.success("Interest request accepted."));
    }

    /**
     * Decline a received interest request.
     */
    @PutMapping("/{interestId}/decline")
    public ResponseEntity<ApiResponse> declineInterest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long interestId) {

        interestService.declineInterest(userDetails.getUsername(), interestId);
        return ResponseEntity.ok(ApiResponse.success("Interest request declined."));
    }

    /**
     * Get all mutual matches for the logged-in user.
     */
    @GetMapping("/matches")
    public ResponseEntity<List<MatchResponse>> getMatches(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<MatchResponse> matches = interestService.getMatches(userDetails.getUsername());
        return ResponseEntity.ok(matches);
    }
}
