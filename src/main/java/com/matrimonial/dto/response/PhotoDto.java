package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: PhotoDto
 *
 * Represents a single photo in the profile response.
 *
 * WHY THIS EXISTS:
 *   Previously, ProfileResponse only returned List<String> photoUrls.
 *   That meant the frontend/Postman had no way to know the DB id of each photo,
 *   making it impossible to call DELETE /api/profile/photos/{photoId} correctly.
 *
 *   Now each photo carries its own DB id so the caller knows exactly
 *   which id to pass when deleting a specific photo.
 *
 * Fields:
 *   photoId   → DB primary key from profile_photos table (use this in delete URL)
 *   photoUrl  → File path/URL of the photo on disk
 *   isPrimary → true if this is the profile's main/cover photo
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {

    // DB id of this photo — pass this to DELETE /api/profile/photos/{photoId}
    private Long photoId;

    // Relative URL path where photo is stored on disk
    private String photoUrl;

    // Indicates if this is the primary/cover photo for the profile
    private Boolean isPrimary;
}
