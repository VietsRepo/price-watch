package com.vietsrepo.pricewatch.user.repository;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.EMAIL;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.PASSWORD;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.vietsrepo.pricewatch.config.PostgresTestContainerConfig;
import com.vietsrepo.pricewatch.entity.User;
import com.vietsrepo.pricewatch.enums.Role;
import com.vietsrepo.pricewatch.repository.UserRepository;

@DataJpaTest
@ActiveProfiles("test")
@Import(PostgresTestContainerConfig.class)
class UserRepositoryTest {
	
	@Autowired
	private UserRepository repository;
	
	private static final String NON_EXISTING_EMAIL = "non-existing@gmail.com";

	private User buildUser() {
		return User.builder()
				.email(EMAIL)
				.username(USERNAME)
				.password(PASSWORD)
				.role(Role.USER).
				build();
	}
	
	private User persistUser() {
		User user = buildUser();
		return repository.save(user);
	}

	@Nested
	@DisplayName("findByEmailOrUsername()")
	class FindByEmailOrUsername {
		
		@Test
		@DisplayName("Should return a user when only email matches")
		void should_return_user_when_only_email_matches() {
			persistUser();

			assertThat(repository.findByEmailOrUsername(EMAIL))
				.isPresent()
				.get()
				.extracting(User::getEmail).isEqualTo(EMAIL);
		}

		@Test
		@DisplayName("Should return a user when only username matches")
		void should_return_user_when_only_username_matches() {
			persistUser();

			assertThat(repository.findByEmailOrUsername(USERNAME))
				.isPresent()
				.get()
				.extracting(User::getUsername)
				.isEqualTo(USERNAME);
		}

		@Test
		@DisplayName("Should return empty when neither email nor username matches")
		void should_return_empty_when_neither_matches() {
			persistUser();

			assertThat(repository.findByEmailOrUsername("email-username")).isEmpty();
		}
	}
	
	@Nested
	@DisplayName("existsByEmailOrUsername()")
	class ExistsByEmailOrUsername {
		
		@Test
		@DisplayName("Should return true when email exists")
		void should_return_true_when_email_exists() {
			persistUser();
			
			assertThat(repository.existsByEmail(EMAIL)).isTrue();
		}
		
		@Test
		@DisplayName("Should return false when email does not exists")
		void should_return_false_when_email_does_not_exists() {
			persistUser();
			
			assertThat(repository.existsByEmail(NON_EXISTING_EMAIL)).isFalse();
		}
	}
}
