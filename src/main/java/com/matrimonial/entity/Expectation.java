package com.matrimonial.entity;

import com.matrimonial.entity.enums.Diet;
import com.matrimonial.entity.enums.MaritalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ENTITY: Expectation
 *
 * Maps to the "expectations" table.
 * Stores what a user is looking for in a partner — all fields optional.
 * One user = One expectation row (OneToOne).
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(name = "expectations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expectation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One user = One expectation record
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // ── Age range ──
    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    // ── Marital status preference ──
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_marital_status", length = 20)
    private MaritalStatus preferredMaritalStatus;

    // ── Height range (stored as formatted string e.g. "5'4"") ──
    @Column(name = "preferred_min_height", length = 20)
    private String preferredMinHeight;

    @Column(name = "preferred_max_height", length = 20)
    private String preferredMaxHeight;

    // ── Location ──
    @Column(name = "preferred_city", length = 100)
    private String preferredCity;

    // ── Education & Profession ──
    @Column(name = "preferred_education", length = 150)
    private String preferredEducation;

    @Column(name = "preferred_profession", length = 150)
    private String preferredProfession;

    // ── Income preference (e.g. "40,000 - 80,000/month") ──
    @Column(name = "preferred_income", length = 100)
    private String preferredIncome;

    // ── Community ──
    @Column(name = "preferred_gotra", length = 100)
    private String preferredGotra;

    // ── Diet preference ──
    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_diet", length = 20)
    private Diet preferredDiet;

    // ── Religion ──
    @Column(name = "preferred_religion", length = 100)
    private String preferredReligion;

    // ── Free text — any other expectations ──
    @Column(name = "about_expectations", columnDefinition = "TEXT")
    private String aboutExpectations;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
