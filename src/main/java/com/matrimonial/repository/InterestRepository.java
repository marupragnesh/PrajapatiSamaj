package com.matrimonial.repository;

import com.matrimonial.entity.InterestRequest;
import com.matrimonial.entity.InterestRequest.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITORY: InterestRepository
 *
 * Handles all DB operations for the "interest_requests" table.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface InterestRepository extends JpaRepository<InterestRequest, Long> {

    // Check if interest request already exists between two users
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    // Find existing request to check its current status before re-sending
    Optional<InterestRequest> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    // All interest requests received by a user (to show Accept/Decline screen)
    List<InterestRequest> findByReceiverIdAndStatus(Long receiverId, Status status);

    /**
     * Find MUTUAL matches — users who have ACCEPTED each other's interest.
     *
     * A mutual match exists when:
     *   User A sent interest to User B (ACCEPTED)
     *   AND User B also sent interest to User A (ACCEPTED)
     *
     * This query finds all users with whom the current user has mutual acceptance.
     */
    @Query("""
        SELECT ir FROM InterestRequest ir
        WHERE ir.status = 'ACCEPTED'
        AND (
            (ir.sender.id = :userId AND EXISTS (
                SELECT ir2 FROM InterestRequest ir2
                WHERE ir2.sender.id = ir.receiver.id
                AND ir2.receiver.id = :userId
                AND ir2.status = 'ACCEPTED'
            ))
        )
    """)
    List<InterestRequest> findMutualMatches(@Param("userId") Long userId);
}
