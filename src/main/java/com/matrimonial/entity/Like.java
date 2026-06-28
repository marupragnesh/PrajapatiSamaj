package com.matrimonial.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ENTITY: Like
 *
 * Maps to the "likes" table.
 * A user can like another user's profile.
 * Business rules (enforced in service layer):
 *   - Max 3 likes per day (resets at midnight)
 *   - Cannot like same profile twice (UNIQUE KEY on sender + receiver)
 *   - Cannot like own profile
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(
    name = "likes",
    uniqueConstraints = {
        // Prevents a user from liking the same profile more than once
        @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who sent the like
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // The user who received the like
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // Timestamp of when the like was sent
    @Column(name = "liked_at", nullable = false, updatable = false)
    private LocalDateTime likedAt;

    @PrePersist
    protected void onCreate() {
        this.likedAt = LocalDateTime.now();
    }
}
