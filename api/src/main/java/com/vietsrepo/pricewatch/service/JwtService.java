package com.vietsrepo.pricewatch.service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vietsrepo.pricewatch.properties.JwtProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

	private final JwtProperties jwtProperties;

	public String generateToken(UUID userId) {
		Date expiration = Date.from(Instant.now().plus(jwtProperties.getExpiration()));

		return Jwts
				.builder()
				.subject(userId.toString())
				.issuedAt(Date.from(Instant.now()))
				.issuer(jwtProperties.getIssuer())
				.expiration(expiration)
				.signWith(getSigningKey())
				.compact();
	}

	public Claims extractClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSigningKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public UUID extractUserId(String token) {
		String subject = extractClaims(token).getSubject();
		return UUID.fromString(subject);
	}
	
	// TODO: check token revocation (Redis) once implemented NOSONAR
	public boolean isTokenValid(UserDetails userDetails) {
		if (!userDetails.isEnabled()) {
			return false;
		}
		
		if (!userDetails.isAccountNonLocked()) {
			return false;
		}
		
		return true;
	}
	
	public long getExpirationSeconds() {
		return jwtProperties.getExpiration().toSeconds();
	}

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
	}
}
