
package com.backend.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "auth_sessions",
    indexes = {
        @Index(
            name = "idx_auth_sessions_token_hash",
            columnList = "token_hash"
        )
    }
)
public class AuthSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Store only the token hash, never the original bearer token.
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    // Each session belongs to one registered user.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AuthSession() {
    }

    @PrePersist
    protected void onCreate() {
        // Record when the session was created.
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
