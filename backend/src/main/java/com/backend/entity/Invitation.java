package com.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_invitations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Invitation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=150) private String email;
    @Column(nullable=false, length=30) private String role;
    @Column(nullable=false, unique=true, length=64) private String tokenHash;
    @Column(nullable=false) private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private Long createdBy;
    @Column(nullable=false, updatable=false) private LocalDateTime createdAt;
    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
