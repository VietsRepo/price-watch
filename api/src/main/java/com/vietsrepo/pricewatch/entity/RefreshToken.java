package com.vietsrepo.pricewatch.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@Getter
@Setter
public class RefreshToken {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(updatable = false)
	private UUID id;

	private String tokenHash;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(name = "created_at", updatable = false, insertable = false)
	private Instant createdAt;

	@Column(name = "revoked_at")
	private Instant revokedAt;
}
