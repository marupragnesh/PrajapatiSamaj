package com.matrimonial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION: ResourceNotFoundException
 *
 * Thrown when a requested entity is not found in the database.
 * Examples:
 *   - User not found by ID
 *   - Profile not found for a user
 *   - Interest request not found
 *
 * Automatically maps to HTTP 404 Not Found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
