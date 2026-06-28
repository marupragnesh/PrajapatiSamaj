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

    // List of photo URLs to display
    private List<String> photoUrls;

    // Which photo is the primary/cover photo
    private String primaryPhotoUrl;
}
