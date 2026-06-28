package com.matrimonial.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ENTITY: User
 *
 * Maps to the "users" table in the database.
 * Stores only authentication-related data (email + password).
 * Profile data is in the Profile entity (separate table).
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // Primary key — auto-incremented by MySQL
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // User's email — must be unique and not null
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // BCrypt-hashed password — never store plain text
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    // Admin can deactivate a user account (soft delete)
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    // Timestamp when the account was created
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Timestamp when the account was last updated
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Automatically set createdAt before first save
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Automatically update updatedAt before every update
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
