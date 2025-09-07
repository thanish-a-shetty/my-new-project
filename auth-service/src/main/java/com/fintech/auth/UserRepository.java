package com.fintech.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email address
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a user exists with the given email
     * @param email the email address to check
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Find user by email and enabled status
     * @param email the email address to search for
     * @param enabled the enabled status to filter by
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmailAndEnabled(String email, boolean enabled);

    /**
     * Count users by enabled status
     * @param enabled the enabled status to count
     * @return number of users with the specified enabled status
     */
    long countByEnabled(boolean enabled);
}
