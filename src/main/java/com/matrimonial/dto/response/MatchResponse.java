package com.matrimonial.dto.response;

import com.matrimonial.entity.Profile.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO: MatchResponse
 *
 * Returned when listing mutual matches (both users accepted each other's interest).
 * Shows the matched user's profile info.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchResponse {

    // The interest request ID (useful for reference)
    private Long interestRequestId;

    // Matched user's profile details
    private Long profileId;
    private Long userId;
    private String fullName;
    private Integer age;
    private Gender gender;
    private String city;
    private String education;
    private String profession;
    private String religion;
    private String hobbies;
    private List<String> photoUrls;
    private String primaryPhotoUrl;

    // When the match happened (when interest was accepted)
    private LocalDateTime matchedAt;
}
