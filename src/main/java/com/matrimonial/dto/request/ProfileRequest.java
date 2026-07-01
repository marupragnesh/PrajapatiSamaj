package com.matrimonial.dto.request;

import com.matrimonial.entity.Profile.Gender;
import com.matrimonial.entity.enums.Diet;
import com.matrimonial.entity.enums.MaritalStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO: ProfileRequest
 *
 * Used when creating or updating a user's profile.
 * Required fields are validated before reaching the service layer.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class ProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String fullName;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 80, message = "Age must not exceed 80")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Marital status is required")
    private MaritalStatus maritalStatus;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    // Required — 10 digit Indian mobile number, numeric only
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9][0-9]{9}$", message = "Enter a valid 10-digit mobile number")
    private String mobileNo;

    // Required — address split into 3 fields
    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String addressLine;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Enter a valid 6-digit pincode")
    private String pincode;

    @NotBlank(message = "Education is required")
    @Size(max = 150, message = "Education must not exceed 150 characters")
    private String education;

    @NotBlank(message = "Profession is required")
    @Size(max = 150, message = "Profession must not exceed 150 characters")
    private String profession;

    // ── Optional fields ──

    @Size(max = 20, message = "Height must not exceed 20 characters")
    private String height;         // e.g. "5'8\""

    @Size(max = 100, message = "Income must not exceed 100 characters")
    private String income;         // e.g. "50,000/month"

    @Size(max = 100, message = "Gotra must not exceed 100 characters")
    private String gotra;

    private Diet diet;             // VEG, NON_VEG, VEGAN

    @Size(max = 100, message = "Religion must not exceed 100 characters")
    private String religion;

    private String hobbies;
}
