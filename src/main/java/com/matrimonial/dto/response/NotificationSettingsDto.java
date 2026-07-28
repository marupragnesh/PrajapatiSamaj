package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO: NotificationSettingsDto
 *
 * Carries email notification preferences for the logged-in user.
 *
 * Layer: DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingsDto {

    private Boolean emailOnLike;
    private Boolean emailOnInterest;
    private Boolean emailOnAcceptInterest;
}
