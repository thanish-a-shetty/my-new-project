package com.fintech.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    /**
     * Find verification token by token string
     * @param token the token string to search for
     * @return Optional containing the verification token if found, empty otherwise
     */
    Optional<VerificationToken> findByToken(String token);

    /**
     * Find verification token by user
     * @param user the user to search for
     * @return Optional containing the verification token if found, empty otherwise
     */
    Optional<VerificationToken> findByUser(User user);

    /**
     * Find verification token by user and used status
     * @param user the user to search for
     * @param used the used status to filter by
     * @return Optional containing the verification token if found, empty otherwise
     */
    Optional<VerificationToken> findByUserAndUsed(User user, boolean used);

    /**
     * Delete verification tokens by user
     * @param user the user whose tokens should be deleted
     */
    void deleteByUser(User user);

    /**
     * Count verification tokens by user
     * @param user the user to count tokens for
     * @return number of verification tokens for the user
     */
    long countByUser(User user);
}
