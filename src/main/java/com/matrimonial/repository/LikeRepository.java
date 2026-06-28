package com.matrimonial.repository;

import com.matrimonial.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REPOSITORY: LikeRepository
 *
 * Handles all DB operations for the "likes" table.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    // Check if user already liked this profile (prevents duplicate likes)
    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    /**
     * Count how many likes the sender has sent TODAY.
     * Used to enforce the 3-likes-per-day free limit.
     */
    int countBySenderIdAndLikedAtAfter(Long senderId, LocalDateTime startOfDay);

    // Get all likes received by a user — shown in "Who liked me?" section
    List<Like> findByReceiverId(Long receiverId);

    // Delete all likes sent BY this user — used in account deletion
    void deleteBySenderId(Long senderId);

    // Delete all likes received BY this user — used in account deletion
    void deleteByReceiverId(Long receiverId);
}
