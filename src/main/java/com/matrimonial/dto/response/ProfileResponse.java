package com.matrimonial.dto.response;

import com.matrimonial.entity.Profile.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO: ProfileResponse
 *
 * Returned when viewing a profile (own or another user's).
 * IMPORTANT: This is the FULL profile view.
 *
 * For the "who liked me" safe view, use LikerSafeView instead
 * (which strips out email/contact info).
 *
 * CHANGE: Replaced List<String> photoUrls with List<PhotoDto> photos.
 *   Each PhotoDto now carries photoId + photoUrl + isPrimary.
 *   This lets the caller know the exact DB id to use when deleting a photo.
 *   primaryPhotoUrl is kept for quick access to the cover photo URL.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

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
    private Boolean isComplete;

    // List of photos — each item has photoId, photoUrl, isPrimary
    // Use photoId from here to call DELETE /api/profile/photos/{photoId}
    private List<PhotoDto> photos;

    // Quick access to the primary/cover photo URL (null if no photos)
    private String primaryPhotoUrl;
}
