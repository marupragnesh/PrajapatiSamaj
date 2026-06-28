package com.matrimonial.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ENTITY: Profile
 *
 * Maps to the "profiles" table.
 * Each user has exactly ONE profile (OneToOne relationship).
 * Profile must be complete (is_complete = true) to appear in discovery.
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One user = One profile (FK: user_id → users.id)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private Integer age;

    // ENUM values: MALE, FEMALE, PREFER_NOT_TO_SAY
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 150)
    private String education;

    @Column(nullable = false, length = 150)
    private String profession;

    // Religion is optional (nullable)
    @Column(length = 100)
    private String religion;

    // Hobbies are optional, stored as text
    @Column(columnDefinition = "TEXT")
    private String hobbies;

    // Profile only shows in discovery when this is true
    @Column(name = "is_complete", nullable = false)
    @Builder.Default
    private Boolean isComplete = false;

    // One profile can have up to 5 photos
    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProfilePhoto> photos;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ===== ENUM for Gender =====
    public enum Gender {
        MALE,
        FEMALE,
        PREFER_NOT_TO_SAY
    }
}
