package com.vietsrepo.pricewatch.service;

import java.time.Instant;
import java.util.Date;

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

	public String generateToken(String username) {
		Date expiration = Date.from(Instant.now().plusMillis(jwtProperties.getExpiration()));

		return Jwts
				.builder()
				.subject(username)
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

	public String extractUsername(String token) {
		return extractClaims(token).getSubject();
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

	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
	}
}
