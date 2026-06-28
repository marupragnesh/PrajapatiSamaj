package com.matrimonial.repository;

import com.matrimonial.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * REPOSITORY: UserRepository
 *
 * Handles all DB operations for the "users" table.
 * Extends JpaRepository → gives us save(), findById(), delete() etc. for free.
 *
 * Layer: Repository (only DB queries, no business logic)
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email — used during login and forgot-password
    Optional<User> findByEmail(String email);

    // Check if email already exists — used during registration
    boolean existsByEmail(String email);
}
