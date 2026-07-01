package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: ProfileSearchResultDto
 *
 * Lightweight response for the "search by name" feature.
 * Carries only what the search results list needs — full name + primary
 * photo + profileId for navigation. Deliberately excludes everything else
 * (age, city, etc.) to keep search responses fast and minimal.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSearchResultDto {

    private Long profileId;
    private String fullName;
    private String primaryPhotoUrl; // null if user has not uploaded a photo yet
}
