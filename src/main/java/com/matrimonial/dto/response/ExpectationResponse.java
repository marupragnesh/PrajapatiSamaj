package com.matrimonial.dto.response;

import com.matrimonial.entity.enums.Diet;
import com.matrimonial.entity.enums.MaritalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: ExpectationResponse
 *
 * Returned when viewing expectations — own or another user's profile.
 * All fields are nullable (user may not have filled everything).
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpectationResponse {

    private Integer minAge;
    private Integer maxAge;
    private MaritalStatus preferredMaritalStatus;
    private String preferredMinHeight;
    private String preferredMaxHeight;
    private String preferredCity;
    private String preferredEducation;
    private String preferredProfession;
    private String preferredIncome;
    private String preferredGotra;
    private Diet preferredDiet;
    private String preferredReligion;
    private String aboutExpectations;
}
