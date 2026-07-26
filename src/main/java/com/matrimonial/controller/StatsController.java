package com.matrimonial.controller;

import com.matrimonial.dto.response.StatsResponse;
import com.matrimonial.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CONTROLLER: StatsController
 *
 * REST Controller for platform statistics.
 * Exposes public endpoint to get count of profiles created today.
 *
 * Layer: Controller (REST endpoints only, no direct DB logic)
 */
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final ProfileRepository profileRepository;

    /**
     * GET /api/stats/today-registrations
     * Returns count of user profiles created today.
     */
    @GetMapping("/today-registrations")
    public ResponseEntity<StatsResponse> getTodayRegistrations() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long count = profileRepository.countByCreatedAtGreaterThanEqual(startOfDay);

        StatsResponse response = StatsResponse.builder()
                .todayRegistrationsCount(count)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Stats queried — todayRegistrationsCount={}", count);

        return ResponseEntity.ok(response);
    }
}
