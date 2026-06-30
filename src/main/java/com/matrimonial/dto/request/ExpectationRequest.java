package com.matrimonial.dto.request;

import com.matrimonial.entity.enums.Diet;
import com.matrimonial.entity.enums.MaritalStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO: ExpectationRequest
 *
 * Used when creating or updating partner expectations.
 * All fields are optional — user may fill as many or as few as they like.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class ExpectationRequest {

    @Min(value = 18, message = "Minimum age must be at least 18")
    @Max(value = 80, message = "Minimum age must not exceed 80")
    private Integer minAge;

    @Min(value = 18, message = "Maximum age must be at least 18")
    @Max(value = 80, message = "Maximum age must not exceed 80")
    private Integer maxAge;

    // Optional: SINGLE, DIVORCED, WIDOWED
    private MaritalStatus preferredMaritalStatus;

    // Optional: e.g. "5'4\""
    @Size(max = 20, message = "Min height must not exceed 20 characters")
    private String preferredMinHeight;

    @Size(max = 20, message = "Max height must not exceed 20 characters")
    private String preferredMaxHeight;

    @Size(max = 100, message = "Preferred city must not exceed 100 characters")
    private String preferredCity;

    @Size(max = 150, message = "Preferred education must not exceed 150 characters")
    private String preferredEducation;

    @Size(max = 150, message = "Preferred profession must not exceed 150 characters")
    private String preferredProfession;

    // Optional: e.g. "40,000 - 80,000/month"
    @Size(max = 100, message = "Preferred income must not exceed 100 characters")
    private String preferredIncome;

    @Size(max = 100, message = "Preferred gotra must not exceed 100 characters")
    private String preferredGotra;

    // Optional: VEG, NON_VEG, VEGAN
    private Diet preferredDiet;

    @Size(max = 100, message = "Preferred religion must not exceed 100 characters")
    private String preferredReligion;

    private String aboutExpectations;
}
