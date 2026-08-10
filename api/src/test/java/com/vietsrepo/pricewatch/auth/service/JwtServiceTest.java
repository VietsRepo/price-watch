package com.vietsrepo.pricewatch.auth.service;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.JWT_EXPIRATION;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vietsrepo.pricewatch.properties.JwtProperties;
import com.vietsrepo.pricewatch.security.CustomUserDetails;
import com.vietsrepo.pricewatch.service.JwtService;
import com.vietsrepo.pricewatch.testsupport.auth.UserTestFixtures;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

	@Mock
	private JwtProperties jwtProperties;
	
	@InjectMocks
	private JwtService service;
	
	private static final String INVALID_TOKEN = "invalid-token";
	private static final String ISSUER = "http://localhost:8080";

	private void stubSigningKey() {
		when(jwtProperties.getSecret()).thenReturn("02hK37DjDaeC+QTeRKILpF7v0H0iv9/atFf1eXpjyqk=");
	}

	private void stubJwtProperties() {
		when(jwtProperties.getExpiration()).thenReturn(JWT_EXPIRATION);
		when(jwtProperties.getIssuer()).thenReturn(ISSUER);
		stubSigningKey();
	}
	
	@Nested
	@DisplayName("generateToken()")
	class GenerateToken {

		@Test
		@DisplayName("Sholud return token when generate token success")
		void should_return_token_when_generate_token_success() {
			stubJwtProperties();

			String token = service.generateToken(USER_ID);

			assertThat(token).isNotBlank();
			assertThat(service.extractClaims(token).getExpiration())
					.isCloseTo(Date.from(Instant.now().plus(JWT_EXPIRATION)), 2000);
		}
	}
	
	@Nested
	@DisplayName("extractClaims()")
	class ExtractClaims {

		@Test
		@DisplayName("Should return a claims when token is valid")
		void should_return_claims_when_token_valid() {
			stubJwtProperties();

			String token = service.generateToken(USER_ID);

			Claims claims = service.extractClaims(token);

			assertThat(claims).isNotNull();
			assertThat(claims.getSubject()).isEqualTo(USER_ID.toString());
			assertThat(claims.getIssuedAt()).isCloseTo(Date.from(Instant.now()), 2000);
			assertThat(claims.getIssuer()).isEqualTo(ISSUER);
			assertThat(claims.getExpiration()).isCloseTo(Date.from(Instant.now().plus(JWT_EXPIRATION)), 2000);

		}

		@Test
		@DisplayName("Should throw an IllegalArgumentException when token is invalid")
		void should_throw_illegal_argument_exception_when_token_invalid() {
			stubSigningKey();

			assertThatThrownBy(() -> service.extractClaims(INVALID_TOKEN))
				.isInstanceOf(JwtException.class);

		}
	}
	

	@Nested
	@DisplayName("extractUsername()")
	class ExtractUsername {

		@Test
		@DisplayName("Should return an username when token is valid")
		void should_return_username_when_token_is_valid() {
			stubJwtProperties();

			String token = service.generateToken(USER_ID);

			UUID userId = service.extractUserId(token);

			assertThat(userId).isEqualTo(USER_ID);
		}

		@Test
		@DisplayName("Should throw an IllegalArgumentException when token is invalid")
		void should_throw_illegal_argument_exception_when_token_invalid() {
			stubSigningKey();

			assertThatThrownBy(() -> service.extractUserId(INVALID_TOKEN))
				.isInstanceOf(JwtException.class);
		}
	}
	
	@Nested
	@DisplayName("isTokenValid()")
	class IsTokenValid {

		@Test
		@DisplayName("Should return false when UserDetails is disabled")
		void should_return_false_when_userdetails_is_disabled() {
			CustomUserDetails customUserDetails = new CustomUserDetails(
				UserTestFixtures.defaultUserBuilder().enabled(false).build()
			);

			assertThat(service.isTokenValid(customUserDetails)).isFalse();
		}

		@Test
		@DisplayName("Should return false when UserDetails is account locked")
		void should_return_false_when_userdetails_is_account_locked() {
			CustomUserDetails customUserDetails = new CustomUserDetails(
				UserTestFixtures.defaultUserBuilder().accountLocked(true).build()
			);

			assertThat(service.isTokenValid(customUserDetails)).isFalse();
		}

		@Test
		@DisplayName("Should return true when UserDetails is valid")
		void should_return_true_when_userdetails_is_valid() {
			CustomUserDetails customUserDetails = new CustomUserDetails(
				UserTestFixtures.defaultUserBuilder().build()
			);

			assertThat(service.isTokenValid(customUserDetails)).isTrue();
		}
	}
	
	@Nested
	@DisplayName("getExpirationSeconds()")
	class GetExpirationSeconds {
		
		@Test
		@DisplayName("Should return correct expiration in seconds")
		void should_return_correct_expiration_in_seconds() {
			when(jwtProperties.getExpiration()).thenReturn(JWT_EXPIRATION);
			
			assertThat(service.getExpirationSeconds()).isEqualTo(JWT_EXPIRATION.toSeconds());
		}
	}
	
}
