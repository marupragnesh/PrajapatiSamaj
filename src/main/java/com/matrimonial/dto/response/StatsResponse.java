package com.matrimonial.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO: StatsResponse
 *
 * Returned when querying daily platform registration statistics.
 *
 * Layer: DTO (data transfer only)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {

    private long todayRegistrationsCount;
    private LocalDateTime timestamp;
}
