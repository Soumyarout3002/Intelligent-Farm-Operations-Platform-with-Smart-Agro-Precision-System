
package com.backend.repository;

import com.backend.entity.AuthSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AuthSessionRepository
        extends JpaRepository<AuthSession, Long> {

    // Find an active session using the SHA-256 token hash.
    Optional<AuthSession> findByTokenHashAndRevokedFalse(
            String tokenHash
    );

    // Delete all sessions belonging to a particular user.
    void deleteAllByUser_Id(Long userId);

    
@org.springframework.data.jpa.repository.Modifying
@org.springframework.data.jpa.repository.Query("""
    UPDATE AuthSession s
    SET s.revoked = true
    WHERE s.user.id = :userId
      AND s.revoked = false
    """)
int revokeAllActiveSessionsByUserId(
        @org.springframework.data.repository.query.Param("userId")
        Long userId
);

}
