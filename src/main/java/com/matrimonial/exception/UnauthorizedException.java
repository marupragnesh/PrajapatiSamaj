package com.matrimonial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION: UnauthorizedException
 *
 * Thrown when a user tries to access a resource they don't own.
 * Examples:
 *   - Deleting another user's photo
 *   - Accepting/declining someone else's interest request
 *
 * Automatically maps to HTTP 403 Forbidden.
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
