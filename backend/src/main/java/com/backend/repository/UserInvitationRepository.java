
package com.backend.repository;
import java.util.List;

import com.backend.entity.UserInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserInvitationRepository
        extends JpaRepository<UserInvitation, Long> {
            List<UserInvitation> findAllByEmailIgnoreCaseAndStatus(
        String email,
        String status
);

    Optional<UserInvitation> findByTokenHash(String tokenHash);

    boolean existsByEmailIgnoreCaseAndStatus(
            String email, String status);
}
