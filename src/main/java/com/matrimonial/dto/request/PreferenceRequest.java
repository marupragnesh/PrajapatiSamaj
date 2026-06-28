package com.matrimonial.dto.request;

import com.matrimonial.entity.PartnerPreference.PreferredGender;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO: PreferenceRequest
 *
 * Used when user sets or updates their partner gender preference.
 * This controls what profiles they see in the discovery feed.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
public class PreferenceRequest {

    @NotNull(message = "Preferred gender is required")
    private PreferredGender preferredGender;
}
