package com.matrimonial.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ENTITY: ProfilePhoto
 *
 * Maps to the "profile_photos" table.
 * A profile can have up to 5 photos (enforced in service layer).
 * One photo can be marked as primary (displayed as main photo).
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(name = "profile_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilePhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many photos belong to one profile
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    // URL or local path where photo is stored
    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    // Marks which photo is the profile's main/cover photo
    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private Boolean isPrimary = false;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        this.uploadedAt = LocalDateTime.now();
    }
}
