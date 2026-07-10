package com.vietsrepo.pricewatch.auth.service;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.PASSWORD;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.vietsrepo.pricewatch.enums.Role;
import com.vietsrepo.pricewatch.repository.UserRepository;
import com.vietsrepo.pricewatch.security.CustomUserDetailsService;
import com.vietsrepo.pricewatch.testsupport.auth.UserTestFixtures;;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

	@Mock
	private UserRepository userRepository;
	
	@InjectMocks
	private CustomUserDetailsService service;
	
	@Nested
	@DisplayName("loadUserByUsername()")
	class LoadUserByUsername {
		
		@Test
		@DisplayName("Should return UserDetails when identity match")
		void should_return_user_details_when_identity_match() {
			when(userRepository.findByEmailOrUsername(anyString()))
				.thenReturn(Optional.of(UserTestFixtures.defaultUserBuilder().build()));
			
			UserDetails userDetails = service.loadUserByUsername(USERNAME);
			
			assertThat(userDetails.getUsername()).isEqualTo(USERNAME);
			assertThat(userDetails.getPassword()).isEqualTo(PASSWORD);
			assertThat(userDetails.getAuthorities())
				.extracting(GrantedAuthority::getAuthority)
				.containsExactly("ROLE_" + Role.USER.name());
		}
		
		@Test
		@DisplayName("Should throw an UsernameNotFoundException when identity does not match")
		void should_throw_username_not_found_exception_when_identiy_does_not_match() {
			when(userRepository.findByEmailOrUsername(anyString())).thenReturn(Optional.empty());
			
			assertThatThrownBy(() -> service.loadUserByUsername(USERNAME))
				.isInstanceOf(UsernameNotFoundException.class)
				.hasMessage("User not found with identity: " + USERNAME);
		}
	}
}
