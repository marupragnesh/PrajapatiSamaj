package com.matrimonial.dto.request;

import com.matrimonial.entity.Profile.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO: ProfileRequest
 *
 * Used when creating or updating a user's profile.
 * All required fields are validated before reaching the service layer.
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

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "Education is required")
    @Size(max = 150, message = "Education must not exceed 150 characters")
    private String education;

    @NotBlank(message = "Profession is required")
    @Size(max = 150, message = "Profession must not exceed 150 characters")
    private String profession;

    // Optional fields (nullable)
    @Size(max = 100, message = "Religion must not exceed 100 characters")
    private String religion;

    private String hobbies;
}
