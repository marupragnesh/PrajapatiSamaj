package com.matrimonial.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ENTITY: PartnerPreference
 *
 * Maps to the "partner_preferences" table.
 * Each user sets what gender they want to see in discovery.
 * One user = One preference row (OneToOne).
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(name = "partner_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One user = One preference (FK: user_id → users.id)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // What gender profiles should this user see in discovery?
    // ANY = show both MALE and FEMALE
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_gender", nullable = false, length = 10)
    @Builder.Default
    private PreferredGender preferredGender = PreferredGender.ANY;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== ENUM for Preferred Gender =====
    public enum PreferredGender {
        MALE,
        FEMALE,
        ANY
    }
}
