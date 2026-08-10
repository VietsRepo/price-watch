package com.vietsrepo.pricewatch.auth.service;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.ACCESS_TOKEN;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.EMAIL;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.JWT_EXPIRATION;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.PASSWORD;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.RAW_TOKEN;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.REFRESH_TOKEN;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.REFRESH_TOKEN_EXPIRATION;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.TOKEN_HASH;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USERNAME;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USER_ID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.vietsrepo.pricewatch.dto.AuthResponse;
import com.vietsrepo.pricewatch.dto.LoginRequest;
import com.vietsrepo.pricewatch.dto.RefreshTokenRequest;
import com.vietsrepo.pricewatch.dto.RegisterRequest;
import com.vietsrepo.pricewatch.entity.RefreshToken;
import com.vietsrepo.pricewatch.entity.User;
import com.vietsrepo.pricewatch.enums.ErrorCode;
import com.vietsrepo.pricewatch.enums.Role;
import com.vietsrepo.pricewatch.exception.BusinessException;
import com.vietsrepo.pricewatch.repository.UserRepository;
import com.vietsrepo.pricewatch.security.CustomUserDetails;
import com.vietsrepo.pricewatch.service.AuthService;
import com.vietsrepo.pricewatch.service.JwtService;
import com.vietsrepo.pricewatch.service.RefreshTokenService;
import com.vietsrepo.pricewatch.testsupport.auth.UserTestFixtures;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
	
	@Mock
	private AuthenticationManager authenticationManager;
	
	@Mock
	private Authentication authentication;
	
	@Mock
	private JwtService jwtService;
	
	@Mock
	private RefreshTokenService refreshTokenService;
	
	@Mock
	private UserRepository userRepository;
	
	@Mock
	private BCryptPasswordEncoder encoder;
	
	@InjectMocks
	private AuthService service;
	
	private static final String PASSWORD_ENCODED = "$2a$10$BQJHStfZly2Dfme7EvsdT.mke5AV94lEOMRGh3uj89tYRMbX5v/y6";

	@Nested
	@DisplayName("login()")
	class Login {
		
		@Test
		@DisplayName("Should return a login respone when authenticate is valid")
		void should_return_login_response_when_authenticate_is_valid() {
			LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);

			CustomUserDetails customUserDetails = new CustomUserDetails(UserTestFixtures.defaultUserBuilder().build());

			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
			when(authentication.getPrincipal()).thenReturn(customUserDetails);
			when(jwtService.generateToken(customUserDetails.getId())).thenReturn(ACCESS_TOKEN);
			when(refreshTokenService.create(customUserDetails.getUser())).thenReturn(REFRESH_TOKEN);
			when(jwtService.getExpirationSeconds()).thenReturn(JWT_EXPIRATION.toSeconds());
			when(refreshTokenService.getExpirationSeconds()).thenReturn(REFRESH_TOKEN_EXPIRATION.toSeconds());

			AuthResponse authResponse = service.login(loginRequest);

			assertThat(authResponse).isNotNull();
			assertThat(authResponse.accessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(authResponse.refreshToken()).isEqualTo(REFRESH_TOKEN);
			assertThat(authResponse.expiresIn()).isEqualTo(JWT_EXPIRATION.toSeconds());
			assertThat(authResponse.refreshExpiresIn()).isEqualTo(REFRESH_TOKEN_EXPIRATION.toSeconds());
		}
		
		@Test
		@DisplayName("Should throw an AuthenticationException when authenticate is invalid")
		void should_throw_authentication_exception_when_authenticate_is_invalid() {
			LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD);
			
			when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));
			
			assertThatThrownBy(() -> service.login(loginRequest))
				.isInstanceOf(BadCredentialsException.class);
		}
	}
	
	@Nested
	@DisplayName("register()")
	class Register {
		
		@Test
		@DisplayName("Should throw a BusinessException when email is already taken")
		void should_throw_business_exception_when_email_already_taken() {
			when(userRepository.existsByEmail(EMAIL))
				.thenReturn(true);
			
			RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);
			
			assertThatThrownBy(() -> service.register(request))
				.isInstanceOf(BusinessException.class)
				.satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode()).isEqualTo(ErrorCode.CREDENTIAL_TAKEN));
		}
		
		@Test
		@DisplayName("Should return an RegisterResponse when register success")
		void should_return_user_id_when_register_success() {
			when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
			when(encoder.encode(anyString())).thenReturn(PASSWORD_ENCODED);
			ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
			when(userRepository.save(captor.capture())).thenAnswer(inv -> {
				User user = inv.getArgument(0);
				user.setId(USER_ID);
				return user;
			});
			
			when(jwtService.generateToken(any(UUID.class))).thenReturn(ACCESS_TOKEN);
			when(refreshTokenService.create(any(User.class))).thenReturn(REFRESH_TOKEN);
			when(jwtService.getExpirationSeconds()).thenReturn(JWT_EXPIRATION.toSeconds());
			when(refreshTokenService.getExpirationSeconds()).thenReturn(REFRESH_TOKEN_EXPIRATION.toSeconds());
			
			RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);
			
			AuthResponse authResponse = service.register(request);
			
			User user = captor.getValue();

			assertThat(user.getId()).isNotNull();
			assertThat(user.getEmail()).isEqualTo(EMAIL);
			assertThat(user.getPassword()).isEqualTo(PASSWORD_ENCODED);
			assertThat(user.getRole()).isEqualTo(Role.USER);
			assertThat(authResponse.accessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(authResponse.refreshToken()).isEqualTo(REFRESH_TOKEN);
			assertThat(authResponse.expiresIn()).isEqualTo(JWT_EXPIRATION.toSeconds());
			assertThat(authResponse.refreshExpiresIn()).isEqualTo(REFRESH_TOKEN_EXPIRATION.toSeconds());
		}
	}
	
	@Nested
	@DisplayName("refreshToken()")
	class RefreshTokenMethod {
		
		
		@Test
		@DisplayName("Should revoke old token and issue new tokens when refresh token succeeds")
		void should_return_login_response_with_new_access_token_and_refresh_token_when_refresh_token_success() {
			User user = UserTestFixtures.defaultUserBuilder()
				.id(USER_ID)
				.build();
			RefreshToken refreshToken = RefreshToken.builder()
				.tokenHash(TOKEN_HASH)
				.user(user)
				.expiresAt(Instant.now().plus(REFRESH_TOKEN_EXPIRATION))
				.build();

			when(refreshTokenService.verify(anyString())).thenReturn(refreshToken);
			when(jwtService.generateToken(any(UUID.class))).thenReturn(ACCESS_TOKEN);
			when(refreshTokenService.create(any(User.class))).thenReturn(REFRESH_TOKEN);
			when(jwtService.getExpirationSeconds()).thenReturn(JWT_EXPIRATION.toSeconds());
			when(refreshTokenService.getExpirationSeconds()).thenReturn(REFRESH_TOKEN_EXPIRATION.toSeconds());

			AuthResponse authResponse = service.refreshToken(new RefreshTokenRequest(RAW_TOKEN));
			
			assertThat(authResponse.accessToken()).isEqualTo(ACCESS_TOKEN);
			assertThat(authResponse.refreshToken()).isEqualTo(REFRESH_TOKEN);
			assertThat(authResponse.expiresIn()).isEqualTo(JWT_EXPIRATION.toSeconds());
			assertThat(authResponse.refreshExpiresIn()).isEqualTo(REFRESH_TOKEN_EXPIRATION.toSeconds());
			
			verify(refreshTokenService, times(1)).revoke(RAW_TOKEN);
			verify(refreshTokenService, times(1)).create(user);
		}
	}
	
	@Nested
	@DisplayName("logout()")
	class Logout {
		
		@Test
		@DisplayName("Should call revoke with the provided refresh token when logout")
		void should_call_revoke_with_provided_token_when_logout() {
			RefreshTokenRequest request = new RefreshTokenRequest(RAW_TOKEN);

			service.logout(request);

			verify(refreshTokenService, times(1)).revoke(RAW_TOKEN);
		}
	}
}
