package com.backend.repository;
import com.backend.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByTokenHash(String tokenHash);
}
