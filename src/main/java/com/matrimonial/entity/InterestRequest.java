package com.matrimonial.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ENTITY: InterestRequest
 *
 * Maps to the "interest_requests" table.
 * A user sends an interest request to another user.
 * The receiver can Accept, Decline, or leave it Pending.
 * When both users have accepted each other → it's a Match.
 *
 * Layer: Entity (no business logic here)
 */
@Entity
@Table(name = "interest_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who sent the interest request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // The user who received the interest request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // Current status of the request
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Status status = Status.PENDING;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ===== ENUM for Interest Request Status =====
    public enum Status {
        PENDING,
        ACCEPTED,
        DECLINED
    }
}
