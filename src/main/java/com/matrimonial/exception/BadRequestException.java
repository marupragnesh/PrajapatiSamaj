package com.matrimonial.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * EXCEPTION: BadRequestException
 *
 * Thrown when the request violates a business rule.
 * Examples:
 *   - Email already registered
 *   - Daily like limit exceeded
 *   - Trying to like own profile
 *   - Duplicate interest request
 *   - Photo limit (5) exceeded
 *
 * Automatically maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
