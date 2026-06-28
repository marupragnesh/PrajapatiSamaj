package com.matrimonial.service;

import com.matrimonial.dto.response.LikerSafeView;
import com.matrimonial.entity.*;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE: LikeService
 *
 * Handles all Like-related business logic.
 *
 * Business rules:
 *   - Free user can only send 3 likes per day (resets at midnight)
 *   - Cannot like own profile
 *   - Cannot like the same profile twice
 *   - Receiver sees safe profile only (no contact details)
 *   - Like sends an email notification to receiver
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final EmailService emailService;

    // Free user daily like limit
    private static final int DAILY_LIKE_LIMIT = 3;

    /**
     * Send a Like to another user's profile.
     *
     * @param senderEmail  logged-in user's email
     * @param receiverProfileId  profile ID of the person being liked
     */
    @Transactional
    public void likeProfile(String senderEmail, Long receiverProfileId) {

        // Load the sender (logged-in user)
        User sender = getUserByEmail(senderEmail);

        // Load the receiver's profile
        Profile receiverProfile = profileRepository.findById(receiverProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        User receiver = receiverProfile.getUser();

        // Rule 1: Cannot like your own profile
        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("You cannot like your own profile.");
        }

        // Rule 2: Cannot like the same profile twice
        if (likeRepository.existsBySenderIdAndReceiverId(sender.getId(), receiver.getId())) {
            throw new BadRequestException("You have already liked this profile.");
        }

        // Rule 3: Check daily like limit (3 per day for free users)
        // Count likes sent today (from midnight of today until now)
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        int likesToday = likeRepository.countBySenderIdAndLikedAtAfter(sender.getId(), startOfToday);

        if (likesToday >= DAILY_LIKE_LIMIT) {
            throw new BadRequestException(
                "You have reached your daily like limit (" + DAILY_LIKE_LIMIT + " likes/day). " +
                "Limit resets at midnight."
            );
        }

        // All checks passed — save the like
        Like like = Like.builder()
                .sender(sender)
                .receiver(receiver)
                .build();

        likeRepository.save(like);

        // Send email notification to receiver (async — won't delay response)
        Profile senderProfile = profileRepository.findByUserId(sender.getId()).orElse(null);
        String senderName = (senderProfile != null) ? senderProfile.getFullName() : "Someone";
        emailService.sendLikeNotification(receiver.getEmail(), senderName);
    }

    /**
     * Get all profiles that liked the logged-in user.
     * Returns SAFE view only — no contact details (email, mobile, etc.)
     *
     * @param receiverEmail  logged-in user's email
     * @return list of LikerSafeView DTOs
     */
    public List<LikerSafeView> getLikesReceived(String receiverEmail) {

        User receiver = getUserByEmail(receiverEmail);

        // Get all likes received by this user
        List<Like> likes = likeRepository.findByReceiverId(receiver.getId());

        // Convert each like's sender to a safe view DTO
        return likes.stream()
                .map(like -> buildLikerSafeView(like.getSender()))
                .collect(Collectors.toList());
    }

    // ===== Private Helpers =====

    /**
     * Build LikerSafeView — only includes safe fields (no contact info).
     */
    private LikerSafeView buildLikerSafeView(User likerUser) {

        Profile profile = profileRepository.findByUserId(likerUser.getId())
                .orElse(null);

        if (profile == null) {
            // Liker hasn't created a profile yet — return minimal view
            return LikerSafeView.builder().build();
        }

        List<ProfilePhoto> photos = photoRepository.findByProfileId(profile.getId());
        List<String> photoUrls = photos.stream()
                .map(ProfilePhoto::getPhotoUrl)
                .collect(Collectors.toList());

        String primaryPhotoUrl = photos.stream()
                .filter(ProfilePhoto::getIsPrimary)
                .map(ProfilePhoto::getPhotoUrl)
                .findFirst()
                .orElse(null);

        // Build LikerSafeView — notice: NO email, NO contact info included
        return LikerSafeView.builder()
                .profileId(profile.getId())
                .fullName(profile.getFullName())
                .age(profile.getAge())
                .gender(profile.getGender())
                .education(profile.getEducation())
                .profession(profile.getProfession())
                .religion(profile.getReligion())
                .hobbies(profile.getHobbies())
                .photoUrls(photoUrls)
                .primaryPhotoUrl(primaryPhotoUrl)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
