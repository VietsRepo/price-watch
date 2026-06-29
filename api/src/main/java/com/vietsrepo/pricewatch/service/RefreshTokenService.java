package com.vietsrepo.pricewatch.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vietsrepo.pricewatch.entity.RefreshToken;
import com.vietsrepo.pricewatch.entity.User;
import com.vietsrepo.pricewatch.enums.ErrorCode;
import com.vietsrepo.pricewatch.exception.BusinessException;
import com.vietsrepo.pricewatch.properties.JwtProperties;
import com.vietsrepo.pricewatch.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
	
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private final RefreshTokenRepository repository;
	private final JwtProperties jwtProperties;
	
	public String create(User user) {
		// Generate 32 random bytes (256 bits)
		byte[] random = new byte[32];
		SECURE_RANDOM.nextBytes(random);

		// Encode to URL-safe
		String token = Base64.getUrlEncoder().withoutPadding().encodeToString(random);

		RefreshToken refreshToken = new RefreshToken();
		refreshToken.setUser(user);
		refreshToken.setTokenHash(sha256(token));
		refreshToken.setExpiresAt(Instant.now().plus(jwtProperties.getRefreshTokenExpiration(), ChronoUnit.DAYS));
		
		repository.save(refreshToken);
		
		return token;
	}
	
	public RefreshToken verify(String rawToken) {
		String hash = sha256(rawToken);

		RefreshToken token = repository.findByTokenHash(hash).orElseThrow(
			() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID)
		);

		if (token.getRevokedAt() != null) {
			log.warn("TOKEN_REVOKED: id:{}", token.getUser().getId());
			
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_REVOKED);
		}

		if (token.getExpiresAt().isBefore(Instant.now())) {
			throw new BusinessException(ErrorCode.REFRESH_TOKEN_EXPIRED);
		}

		return token;
	}

	@Transactional
	public void revoke(String rawToken) {
		String hash = sha256(rawToken);

		RefreshToken token = repository.findByTokenHash(hash).orElseThrow(
			() -> new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID)
		);

		if (token.getRevokedAt() != null) {
			return;
		}

		token.setRevokedAt(Instant.now());
	}
	
	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));

			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}
}
