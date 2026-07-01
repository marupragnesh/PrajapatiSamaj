package com.matrimonial.entity;

import com.matrimonial.entity.enums.Diet;
import com.matrimonial.entity.enums.MaritalStatus;
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

    // ENUM values: SINGLE, DIVORCED, WIDOWED — nullable for existing rows
    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus;

    @Column(nullable = false, length = 100)
    private String city;

    // Required — 10-digit Indian mobile number. Masked (e.g. "98********") when
    // shown to other users; full number visible only to the profile owner.
    @Column(name = "mobile_no", length = 10)
    private String mobileNo;

    // Required — address split into 3 structured fields (nullable in DB for old rows)
    @Column(name = "address_line", length = 255)
    private String addressLine;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "pincode", length = 6)
    private String pincode;

    @Column(nullable = false, length = 150)
    private String education;

    @Column(nullable = false, length = 150)
    private String profession;

    // Optional: stored as "5'8"" formatted string
    @Column(name = "height", length = 20)
    private String height;

    // Optional: e.g. "50,000/month"
    @Column(name = "income", length = 100)
    private String income;

    // Optional: Prajapati community gotra
    @Column(name = "gotra", length = 100)
    private String gotra;

    // Optional: VEG, NON_VEG, VEGAN
    @Enumerated(EnumType.STRING)
    @Column(name = "diet", length = 20)
    private Diet diet;

    // Optional
    @Column(length = 100)
    private String religion;

    // Optional — stored as free text
    @Column(columnDefinition = "TEXT")
    private String hobbies;

    // Profile only shows in discovery when this is true
    @Column(name = "is_complete", nullable = false)
    @Builder.Default
    private Boolean isComplete = false;

    // One profile can have up to 10 photos (see ProfileService.MAX_PHOTOS)
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
