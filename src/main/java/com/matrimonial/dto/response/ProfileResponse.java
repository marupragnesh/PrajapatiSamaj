package com.matrimonial.dto.response;

import com.matrimonial.entity.Profile.Gender;
import com.matrimonial.entity.enums.Diet;
import com.matrimonial.entity.enums.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO: ProfileResponse
 *
 * Returned when viewing a profile (own or another user's).
 *
 * Photos: List<PhotoDto> carries photoId + photoUrl + isPrimary.
 * Expectations: nullable — shown when a user has filled them in.
 * mobileNo: masked (e.g. "98********") when viewing another user's profile;
 *           full number only when the viewer is the profile owner.
 *           Masking decision is made in ProfileService — not here.
 * isMobileUnlocked: companion boolean for mobileNo — true when the viewer sees
 *           the real number, false when masked. Lets the frontend show an
 *           Unlock Contact button without parsing the string for asterisks.
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
    private String name;
    private String surname;
    private String fullName;
    private Integer age;
    private Gender gender;
    private MaritalStatus maritalStatus;
    private String city;
    private String mobileNo;       // masked unless viewer is the owner
    private Boolean isMobileUnlocked; // true = real number shown (owner or paid); false = masked. Frontend uses this to decide whether to show the Unlock Contact button.
    private String addressLine;
    private String state;
    private String pincode;
    private String education;
    private String profession;
    private LocalDate dateOfBirth;
    private String birthTime;
    private Integer weight;
    private String bloodGroup;
    private String birthPlace;
    private Boolean hasMangal;
    private Boolean hasSani;
    private String alternateMobileNo;
    private String height;
    private String income;
    private String gotra;
    private Diet diet;
    private String religion;
    private String hobbies;
    private String fatherName;
    private String fatherOccupation;
    private String motherName;
    private String motherOccupation;
    private String description;
    private Boolean isComplete;


    // Interaction status relative to logged-in viewer
    private Boolean isLikedByMe;       // true if viewer already liked this profile
    private String interestStatus;      // NONE, PENDING_SENT, PENDING_RECEIVED, ACCEPTED, DECLINED

    // List of photos — each has photoId, photoUrl, isPrimary
    private List<PhotoDto> photos;

    // Quick access to primary/cover photo URL (null if no photos)
    private String primaryPhotoUrl;

    // Partner expectations — null if user has not filled them in
    private ExpectationResponse expectations;
}
