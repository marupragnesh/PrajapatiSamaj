package com.matrimonial.dto.response;

import com.matrimonial.entity.InterestRequest.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO: InterestResponse
 *
 * Returned when listing received interest requests.
 * Maps InterestRequest entity safely — avoids lazy loading issues.
 *
 * Contains only the sender's basic info needed for Accept/Decline screen.
 *
 * Layer: DTO (data transfer only, no logic)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterestResponse {

    // Interest request ID — used to call accept/decline endpoints
    private Long interestId;

    // Sender's basic info
    private Long senderUserId;
    private Long senderProfileId;
    private String senderFullName;
    private Integer senderAge;
    private String senderCity;
    private String senderProfession;
    private String senderPrimaryPhotoUrl;

    // Current status of the request
    private Status status;

    private LocalDateTime requestedAt;
}
