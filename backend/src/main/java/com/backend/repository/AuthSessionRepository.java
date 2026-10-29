
package com.backend.repository;

import com.backend.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthSessionRepository
        extends JpaRepository<AuthSession, Long> {

    // Find an active session using the SHA-256 token hash.
    Optional<AuthSession> findByTokenHashAndRevokedFalse(
            String tokenHash
    );

    // Delete all sessions belonging to a particular user.
    void deleteAllByUser_Id(Long userId);
}
