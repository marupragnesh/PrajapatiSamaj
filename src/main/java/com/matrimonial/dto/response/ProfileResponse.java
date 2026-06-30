package com.matrimonial.dto.response;

import com.matrimonial.entity.Profile.Gender;
import com.matrimonial.entity.enums.Diet;
import com.matrimonial.entity.enums.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO: ProfileResponse
 *
 * Returned when viewing a profile (own or another user's).
 *
 * Photos: List<PhotoDto> carries photoId + photoUrl + isPrimary.
 * Expectations: nullable — shown when a user has filled them in.
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
    private MaritalStatus maritalStatus;
    private String city;
    private String education;
    private String profession;
    private String height;
    private String income;
    private String gotra;
    private Diet diet;
    private String religion;
    private String hobbies;
    private Boolean isComplete;

    // List of photos — each has photoId, photoUrl, isPrimary
    private List<PhotoDto> photos;

    // Quick access to primary/cover photo URL (null if no photos)
    private String primaryPhotoUrl;

    // Partner expectations — null if user has not filled them in
    private ExpectationResponse expectations;
}
