package com.matrimonial.dto.response;

import com.matrimonial.entity.Profile.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO: LikerSafeView
 *
 * Returned when a user views "who liked me".
 *
 * SECURITY RULE: Contact info (email, mobile, address) must NEVER appear here.
 * Only safe fields are exposed: Name, Age, Gender, Education, Profession, Religion, Hobbies, Photos.
 *
 * This restriction is enforced at the DTO level, not just service.
 * Even if someone tries to pass more data, this DTO won't carry it.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikerSafeView {

    // Liker's profile ID (for navigation to their full profile)
    private Long profileId;

    // ✅ Safe fields — allowed to show
    private String fullName;
    private Integer age;
    private Gender gender;
    private String education;
    private String profession;
    private String religion;
    private String hobbies;
    private List<String> photoUrls;
    private String primaryPhotoUrl;

    // ❌ NO email, NO mobile, NO address — intentionally excluded
}
