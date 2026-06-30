package com.matrimonial.entity.enums;

/**
 * ENUM: MaritalStatus
 *
 * Shared by Profile (user's own status) and Expectation (preferred partner status).
 * Placed in enums package to avoid duplication across entities.
 */
public enum MaritalStatus {
    SINGLE,
    DIVORCED,
    WIDOWED
}
