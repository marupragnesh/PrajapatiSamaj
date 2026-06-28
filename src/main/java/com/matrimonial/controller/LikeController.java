package com.matrimonial.controller;

import com.matrimonial.dto.response.ApiResponse;
import com.matrimonial.dto.response.LikerSafeView;
import com.matrimonial.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CONTROLLER: LikeController
 *
 * Handles all Like-related HTTP endpoints:
 *   POST /api/likes/{profileId}  - Like a profile (max 3/day for free users)
 *   GET  /api/likes/received     - View who liked me (safe profile view only)
 *
 * Layer: Controller (HTTP in/out only — no business logic)
 */
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * Like another user's profile.
     *
     * Business rules enforced in service:
     *   - Cannot like own profile
     *   - Cannot like same profile twice
     *   - Max 3 likes per day (free user)
     *
     * @param profileId  the profile ID to like
     */
    @PostMapping("/{profileId}")
    public ResponseEntity<ApiResponse> likeProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long profileId) {

        likeService.likeProfile(userDetails.getUsername(), profileId);
        return ResponseEntity.ok(ApiResponse.success("Profile liked successfully."));
    }

    /**
     * Get all profiles that liked the logged-in user.
     * Returns safe view only — no contact details exposed.
     */
    @GetMapping("/received")
    public ResponseEntity<List<LikerSafeView>> getLikesReceived(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<LikerSafeView> likers = likeService.getLikesReceived(userDetails.getUsername());
        return ResponseEntity.ok(likers);
    }
}
