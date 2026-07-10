package com.vietsrepo.pricewatch.auth.service;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.RAW_TOKEN;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.REFRESH_TOKEN_EXPIRATION;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.TOKEN_HASH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vietsrepo.pricewatch.entity.RefreshToken;
import com.vietsrepo.pricewatch.entity.User;
import com.vietsrepo.pricewatch.enums.ErrorCode;
import com.vietsrepo.pricewatch.exception.BusinessException;
import com.vietsrepo.pricewatch.properties.JwtProperties;
import com.vietsrepo.pricewatch.repository.RefreshTokenRepository;
import com.vietsrepo.pricewatch.service.RefreshTokenService;
import com.vietsrepo.pricewatch.testsupport.auth.UserTestFixtures;
import com.vietsrepo.pricewatch.utils.HashUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
	
	@Mock
	private RefreshTokenRepository repository;
	
	@Mock
	private JwtProperties jwtProperties;

	@InjectMocks
	private RefreshTokenService service;
	
	private RefreshToken buildRefreshToken(String tokenHash, Instant expiresAt, Instant revokedAt) {
		return RefreshToken.builder()
				.tokenHash(tokenHash)
				.user(UserTestFixtures.defaultUserBuilder().build())
				.expiresAt(expiresAt)
				.revokedAt(revokedAt)
				.build();
	}
	
	@Nested
	@DisplayName("create()")
	class Create {
		
		@Test
		@DisplayName("Should save token hash derived from returned raw token")
		void should_save_token_hash_derived_from_returned_raw_token() {
			when(jwtProperties.getRefreshTokenExpiration()).thenReturn(REFRESH_TOKEN_EXPIRATION);

			ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
			when(repository.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));
			
			User user = UserTestFixtures.defaultUserBuilder().build();
			
			String rawToken = service.create(user);

			assertThat(rawToken).isNotBlank();

			RefreshToken saved = captor.getValue();
			
			assertThat(saved.getTokenHash()).isEqualTo(HashUtils.sha256(rawToken));
			assertThat(saved.getUser()).isEqualTo(user);
			assertThat(saved.getExpiresAt()).isCloseTo(Instant.now().plus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.DAYS),
					within(2, ChronoUnit.SECONDS));

			verify(repository, times(1)).save(any(RefreshToken.class));
		}
	}
	
	@Nested
	@DisplayName("verify()")
	class Verify {
		
		@Test
		@DisplayName("Should throw a BusinessException when refresh token is invalid")
		void should_throw_business_exception_when_refresh_token_invalid() {
			when(repository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());
			
			assertThatThrownBy(() -> service.verify(RAW_TOKEN))
				.isInstanceOf(BusinessException.class)
				.satisfies(ex -> 
					assertThat(
						((BusinessException) ex).getErrorCode()
					).isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID)
				);
		}
		
		@Test
		@DisplayName("Should throw a BusinessException when refresh token is revoked")
		void should_throw_business_exception_when_refresh_token_revoked() {
			RefreshToken refreshToken = buildRefreshToken(
				TOKEN_HASH,
				Instant.now().plus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.DAYS),
				Instant.now().plus(1, ChronoUnit.DAYS)
			);

			when(repository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(refreshToken));
			
			assertThatThrownBy(() -> service.verify(RAW_TOKEN))
				.isInstanceOf(BusinessException.class)
				.satisfies(ex -> 
					assertThat(
						((BusinessException) ex).getErrorCode()
					).isEqualTo(ErrorCode.REFRESH_TOKEN_REVOKED)
				);
		}
		
		@Test
		@DisplayName("Should throw a BusinessException when refresh token is expired")
		void should_throw_business_exception_when_refresh_token_expired() {
			
			RefreshToken refreshToken = buildRefreshToken(
				TOKEN_HASH,
				Instant.now().minus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.DAYS),
				null
			);

			when(repository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(refreshToken));
			
			assertThatThrownBy(() -> service.verify(RAW_TOKEN))
				.isInstanceOf(BusinessException.class)
				.satisfies(ex -> 
					assertThat(
						((BusinessException) ex).getErrorCode()
					).isEqualTo(ErrorCode.REFRESH_TOKEN_EXPIRED)
				);
		}
		
		@Test
		@DisplayName("Should return a token hash when verify passed")
		void should_return_token_hash_when_verify_passed() {
			RefreshToken refreshToken = buildRefreshToken(
				TOKEN_HASH,
				Instant.now().plus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.DAYS),
				null
			);

			when(repository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(refreshToken));
			
			assertThat(service.verify(RAW_TOKEN)).isEqualTo(refreshToken);
		}
	}
	
	
	@Nested
	@DisplayName("revoke()")
	class Revoke {
		
		@Test
		@DisplayName("Should throw a BusinessException when refresh token is invalid")
		void should_throw_business_exception_when_refresh_token_invalid() {
			when(repository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.empty());
			
			assertThatThrownBy(() -> service.revoke(RAW_TOKEN))
				.isInstanceOf(BusinessException.class)
				.satisfies(ex -> 
					assertThat(
						((BusinessException) ex).getErrorCode()
					).isEqualTo(ErrorCode.REFRESH_TOKEN_INVALID)
				);
		}
		
		@Test
		@DisplayName("Should not update revokedAt when already revoked")
		void should_not_update_revoked_at_when_already_revoked() {
			RefreshToken refreshToken = buildRefreshToken(
				TOKEN_HASH,
				Instant.now().plus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.DAYS),
				Instant.now().plus(1, ChronoUnit.DAYS)
			);

			when(repository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(refreshToken));
			
			service.revoke(RAW_TOKEN);
			
			assertThat(refreshToken.getRevokedAt())
				.isCloseTo(Instant.now().plus(1, ChronoUnit.DAYS), within(2, ChronoUnit.SECONDS));
		}
		
		@Test
		@DisplayName("Should update revokedAt when revokedAt is null")
		void should_update_revoked_at_when_revoked_at_is_null() {
			RefreshToken refreshToken = buildRefreshToken(
				TOKEN_HASH,
				Instant.now().plus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.DAYS),
				null
			);

			when(repository.findByTokenHash(TOKEN_HASH)).thenReturn(Optional.of(refreshToken));

			service.revoke(RAW_TOKEN);

			assertThat(refreshToken.getRevokedAt())
				.isCloseTo(Instant.now(), within(2, ChronoUnit.SECONDS));
		}
	}

}
