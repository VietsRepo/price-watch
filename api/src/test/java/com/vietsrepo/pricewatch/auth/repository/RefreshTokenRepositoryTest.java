package com.vietsrepo.pricewatch.auth.repository;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.REFRESH_TOKEN_EXPIRATION;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.TOKEN_HASH;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.vietsrepo.pricewatch.config.PostgresTestContainerConfig;
import com.vietsrepo.pricewatch.entity.RefreshToken;
import com.vietsrepo.pricewatch.entity.User;
import com.vietsrepo.pricewatch.repository.RefreshTokenRepository;
import com.vietsrepo.pricewatch.repository.UserRepository;
import com.vietsrepo.pricewatch.testsupport.auth.UserTestFixtures;;

@DataJpaTest
@ActiveProfiles("test")
@Import(PostgresTestContainerConfig.class)
class RefreshTokenRepositoryTest {

	@Autowired
	private RefreshTokenRepository repository;
	
	@Autowired
	private UserRepository userRepository;
	
	private static final String NON_MATCHING_TOKEN_HASH = "47358b25a5b1225408e6eef12ec35c2333283ac25fd59a46cd6f36a68a94fdbb";
	
	private RefreshToken persistRefreshToken() {
		User user = UserTestFixtures.defaultUserBuilder().build();
		userRepository.save(user);
		
		RefreshToken refreshToken = RefreshToken.builder()
			.tokenHash(TOKEN_HASH)
			.user(user)
			.expiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION))
			.build();

		return repository.save(refreshToken);
	}
	
	@Nested
	@DisplayName("findByTokenHash()")
	class FindByTokenHash {
		
		@Test
		@DisplayName("Should return a refresh token when token hash matches")
		void should_return_refresh_token_when_token_hash_matches() {
			RefreshToken refreshTokenSaved = persistRefreshToken();
			
			assertThat(repository.findByTokenHash(TOKEN_HASH))
				.isPresent()
				.get()
				.satisfies(refreshToken -> {
					assertThat(refreshToken.getTokenHash()).isEqualTo(TOKEN_HASH);
					assertThat(refreshToken.getUser().getId()).isEqualTo(refreshTokenSaved.getUser().getId());
				});
		}

		@Test
		@DisplayName("Should return empty when token hash does not match")
		void should_return_empty_when_token_hash_does_not_match() {
			persistRefreshToken();
			
			assertThat(repository.findByTokenHash(NON_MATCHING_TOKEN_HASH)).isEmpty();
		}
	}
}
