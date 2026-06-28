package com.matrimonial.service;

import com.matrimonial.dto.response.InterestResponse;
import com.matrimonial.dto.response.MatchResponse;
import com.matrimonial.entity.*;
import com.matrimonial.exception.BadRequestException;
import com.matrimonial.exception.ResourceNotFoundException;
import com.matrimonial.exception.UnauthorizedException;
import com.matrimonial.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SERVICE: InterestService
 *
 * Handles all Interest Request business logic:
 *   - Send an interest request
 *   - Accept or decline a received interest
 *   - View received interests (pending)
 *   - View mutual matches (both accepted)
 *
 * Business rules:
 *   - Cannot send interest to own profile
 *   - Cannot re-send if already PENDING or ACCEPTED
 *   - Only receiver can accept/decline
 *   - Mutual match = both users accepted each other
 *
 * Layer: Service (all business logic lives here)
 */
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PhotoRepository photoRepository;
    private final EmailService emailService;

    /**
     * Send an interest request to another user's profile.
     *
     * @param senderEmail        logged-in user's email
     * @param receiverProfileId  target profile ID
     */
    @Transactional
    public void sendInterest(String senderEmail, Long receiverProfileId) {

        User sender = getUserByEmail(senderEmail);

        Profile receiverProfile = profileRepository.findById(receiverProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile not found."));

        User receiver = receiverProfile.getUser();

        // Rule: Cannot send interest to yourself
        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("You cannot send interest to your own profile.");
        }

        // Rule: Cannot re-send if request already exists (PENDING or ACCEPTED)
        if (interestRepository.existsBySenderIdAndReceiverId(sender.getId(), receiver.getId())) {
            InterestRequest existing = interestRepository
                    .findBySenderIdAndReceiverId(sender.getId(), receiver.getId()).get();

            if (existing.getStatus() == InterestRequest.Status.PENDING) {
                throw new BadRequestException("You have already sent an interest request. Waiting for response.");
            }
            if (existing.getStatus() == InterestRequest.Status.ACCEPTED) {
                throw new BadRequestException("Your interest is already accepted!");
            }
            // If DECLINED — allow re-sending (user can try again)
        }

        // Build and save the interest request
        InterestRequest interestRequest = InterestRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(InterestRequest.Status.PENDING)
                .build();

        interestRepository.save(interestRequest);

        // Notify receiver via email (async)
        Profile senderProfile = profileRepository.findByUserId(sender.getId()).orElse(null);
        String senderName = (senderProfile != null) ? senderProfile.getFullName() : "Someone";
        emailService.sendInterestNotification(receiver.getEmail(), senderName);
    }

    /**
     * Accept an incoming interest request.
     *
     * Business rules:
     *   - Only the RECEIVER of the request can accept
     *   - Request must currently be PENDING
     *
     * @param receiverEmail  logged-in user's email (must be the receiver)
     * @param interestId     the interest request ID
     */
    @Transactional
    public void acceptInterest(String receiverEmail, Long interestId) {

        User receiver = getUserByEmail(receiverEmail);

        InterestRequest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest request not found."));

        // Only the receiver can accept
        if (!interest.getReceiver().getId().equals(receiver.getId())) {
            throw new UnauthorizedException("You are not authorized to accept this request.");
        }

        // Can only accept PENDING requests
        if (interest.getStatus() != InterestRequest.Status.PENDING) {
            throw new BadRequestException("This request has already been " + interest.getStatus().name().toLowerCase() + ".");
        }

        interest.setStatus(InterestRequest.Status.ACCEPTED);
        interestRepository.save(interest);

        // Notify the sender that their interest was accepted (async)
        Profile receiverProfile = profileRepository.findByUserId(receiver.getId()).orElse(null);
        String receiverName = (receiverProfile != null) ? receiverProfile.getFullName() : "Someone";
        emailService.sendInterestAcceptedNotification(interest.getSender().getEmail(), receiverName);
    }

    /**
     * Decline an incoming interest request.
     *
     * Business rules:
     *   - Only the RECEIVER of the request can decline
     *   - Request must currently be PENDING
     *
     * @param receiverEmail  logged-in user's email
     * @param interestId     the interest request ID
     */
    @Transactional
    public void declineInterest(String receiverEmail, Long interestId) {

        User receiver = getUserByEmail(receiverEmail);

        InterestRequest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest request not found."));

        // Only the receiver can decline
        if (!interest.getReceiver().getId().equals(receiver.getId())) {
            throw new UnauthorizedException("You are not authorized to decline this request.");
        }

        // Can only decline PENDING requests
        if (interest.getStatus() != InterestRequest.Status.PENDING) {
            throw new BadRequestException("This request has already been " + interest.getStatus().name().toLowerCase() + ".");
        }

        interest.setStatus(InterestRequest.Status.DECLINED);
        interestRepository.save(interest);
    }

    /**
     * Get all PENDING interest requests received by the logged-in user.
     *
     * Fix: Returns InterestResponse DTO instead of raw InterestRequest entity.
     * Raw entity has LAZY-loaded sender/receiver fields — Jackson cannot serialize
     * them outside a transaction, causing empty or broken JSON response.
     *
     * @param receiverEmail  logged-in user's email
     * @return list of InterestResponse DTOs
     */
    public List<InterestResponse> getReceivedInterests(String receiverEmail) {
        User receiver = getUserByEmail(receiverEmail);

        List<InterestRequest> interests = interestRepository.findByReceiverIdAndStatus(
                receiver.getId(), InterestRequest.Status.PENDING);

        // Map each entity to a safe DTO — resolves lazy loading inside the transaction
        return interests.stream()
                .map(this::buildInterestResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all mutual matches for the logged-in user.
     * A match = both users have ACCEPTED each other's interest.
     *
     * @param userEmail  logged-in user's email
     * @return list of MatchResponse DTOs
     */
    public List<MatchResponse> getMatches(String userEmail) {
        User user = getUserByEmail(userEmail);

        List<InterestRequest> mutualMatches = interestRepository.findMutualMatches(user.getId());

        return mutualMatches.stream()
                .map(interest -> {
                    // Show the OTHER person's profile — not the logged-in user
                    User matchedUser = interest.getReceiver();
                    return buildMatchResponse(interest, matchedUser);
                })
                .collect(Collectors.toList());
    }

    // ===== Private Helpers =====

    /**
     * Maps InterestRequest entity → InterestResponse DTO.
     * Eagerly reads sender fields inside the transaction to avoid lazy loading issues.
     */
    private InterestResponse buildInterestResponse(InterestRequest interest) {

        User sender = interest.getSender();

        // Load sender's profile for display info
        Profile senderProfile = profileRepository.findByUserId(sender.getId()).orElse(null);

        // Load sender's primary photo
        String primaryPhotoUrl = null;
        if (senderProfile != null) {
            primaryPhotoUrl = photoRepository.findByProfileId(senderProfile.getId())
                    .stream()
                    .filter(ProfilePhoto::getIsPrimary)
                    .map(ProfilePhoto::getPhotoUrl)
                    .findFirst()
                    .orElse(null);
        }

        return InterestResponse.builder()
                .interestId(interest.getId())
                .senderUserId(sender.getId())
                .senderProfileId(senderProfile != null ? senderProfile.getId() : null)
                .senderFullName(senderProfile != null ? senderProfile.getFullName() : null)
                .senderAge(senderProfile != null ? senderProfile.getAge() : null)
                .senderCity(senderProfile != null ? senderProfile.getCity() : null)
                .senderProfession(senderProfile != null ? senderProfile.getProfession() : null)
                .senderPrimaryPhotoUrl(primaryPhotoUrl)
                .status(interest.getStatus())
                .requestedAt(interest.getRequestedAt())
                .build();
    }

    private MatchResponse buildMatchResponse(InterestRequest interest, User matchedUser) {

        Profile profile = profileRepository.findByUserId(matchedUser.getId()).orElse(null);

        if (profile == null) {
            return MatchResponse.builder()
                    .interestRequestId(interest.getId())
                    .matchedAt(interest.getUpdatedAt())
                    .build();
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

        return MatchResponse.builder()
                .interestRequestId(interest.getId())
                .profileId(profile.getId())
                .userId(matchedUser.getId())
                .fullName(profile.getFullName())
                .age(profile.getAge())
                .gender(profile.getGender())
                .city(profile.getCity())
                .education(profile.getEducation())
                .profession(profile.getProfession())
                .religion(profile.getReligion())
                .hobbies(profile.getHobbies())
                .photoUrls(photoUrls)
                .primaryPhotoUrl(primaryPhotoUrl)
                .matchedAt(interest.getUpdatedAt())
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
    }
}
