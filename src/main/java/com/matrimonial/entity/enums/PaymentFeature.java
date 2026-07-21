package com.matrimonial.entity.enums;

/**
 * ENUM: PaymentFeature
 *
 * Identifies WHAT a payment unlocks. New locked features are added here
 * as new values only — PaymentService.hasUnlocked(user, feature) then
 * gates any feature the same way, with no other code changes needed.
 *
 * CONTACT_UNLOCK    — account-wide: pay once, see every profile's full mobile number
 * DISCOVER_FILTERS  — reserved for future use (not yet enforced anywhere)
 */
public enum PaymentFeature {
    CONTACT_UNLOCK,
    DISCOVER_FILTERS
}
