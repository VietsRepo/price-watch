package com.vietsrepo.pricewatch.auth.controller;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.ACCESS_TOKEN;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.EMAIL;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.JWT_EXPIRATION;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.PASSWORD;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.RAW_TOKEN;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.REFRESH_TOKEN;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.REFRESH_TOKEN_EXPIRATION;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USERNAME;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.vietsrepo.pricewatch.config.SecurityConfig;
import com.vietsrepo.pricewatch.controller.AuthController;
import com.vietsrepo.pricewatch.dto.AuthResponse;
import com.vietsrepo.pricewatch.dto.LoginRequest;
import com.vietsrepo.pricewatch.dto.RefreshTokenRequest;
import com.vietsrepo.pricewatch.dto.RegisterRequest;
import com.vietsrepo.pricewatch.dto.ValidationMessages;
import com.vietsrepo.pricewatch.enums.ErrorCode;
import com.vietsrepo.pricewatch.exception.BusinessException;
import com.vietsrepo.pricewatch.security.CustomUserDetailsService;
import com.vietsrepo.pricewatch.security.JwtAuthEntryPoint;
import com.vietsrepo.pricewatch.service.AuthService;
import com.vietsrepo.pricewatch.service.JwtService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private AuthService service;
	
	@MockitoBean
	private JwtAuthEntryPoint jwtAuthEntryPoint;
	
	@MockitoBean
	private JwtService jwtService;
	
	@MockitoBean
	private CustomUserDetailsService customUserDetailsService; 
	
	@Nested
	@DisplayName("POST /api/auth/login")
	class Login {
		
		@Test
		@DisplayName("Should return 200 when user login successfully")
		void should_return_200_when_user_login_successfully() throws JacksonException, Exception {
			LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
			AuthResponse response = new AuthResponse(
				ACCESS_TOKEN,
				REFRESH_TOKEN,
				JWT_EXPIRATION.toSeconds(),
				REFRESH_TOKEN_EXPIRATION.toSeconds()
			);
		
			when(service.login(request)).thenReturn(response);
			
			mockMvc.perform(
				post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
			.andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN))
			.andExpect(jsonPath("$.expiresIn").value(JWT_EXPIRATION.toSeconds()))
			.andExpect(jsonPath("$.refreshExpiresIn").value(REFRESH_TOKEN_EXPIRATION.toSeconds()));
		}
		
		@Test
		@DisplayName("Should return 400 when username is blank")
		void should_return_400_when_username_is_blank() throws JacksonException, Exception {
			LoginRequest request = new LoginRequest(" ", PASSWORD);
			
			mockMvc.perform(
				post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors[0].message").value(ValidationMessages.USERNAME_BLANK));
		}
		
		@Test
		@DisplayName("Should return 400 when password is blank")
		void should_return_400_when_password_is_blank() throws JacksonException, Exception {
			LoginRequest request = new LoginRequest(USERNAME, " ");
			
			mockMvc.perform(
				post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors[0].message").value(ValidationMessages.PASSWORD_BLANK));
		}
		
		@Test
		@DisplayName("Should return 400 when username and password are blank")
		void should_return_400_when_username_and_password_are_blank() throws JacksonException, Exception {
			LoginRequest request = new LoginRequest(" ", " ");

			mockMvc.perform(
				post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors[*].message").value(
				containsInAnyOrder(ValidationMessages.USERNAME_BLANK, ValidationMessages.PASSWORD_BLANK)
			));
		}
		
		@Test
		@DisplayName("Should return 401 when user login failed")
		void should_return_401_when_user_login_failed() throws JacksonException, Exception {
			LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
		
			when(service.login(request)).thenThrow(new BadCredentialsException("Bad credentials"));
			
			mockMvc.perform(
				post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.error").value(ErrorCode.INVALID_CREDENTIALS.getStatus().getReasonPhrase()))
			.andExpect(jsonPath("$.code").value(ErrorCode.INVALID_CREDENTIALS.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CREDENTIALS.getMessage()));
		}
	}
	
	@Nested
	@DisplayName("POST /api/auth/register")
	class Register {
		
		@Test
		@DisplayName("Should return 201 when user register successfully")
		void should_return_201_when_user_register_successfully() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);
			AuthResponse response = new AuthResponse(
				ACCESS_TOKEN,
				REFRESH_TOKEN,
				JWT_EXPIRATION.toSeconds(),
				REFRESH_TOKEN_EXPIRATION.toSeconds()
			);
		
			when(service.register(request)).thenReturn(response);
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
			.andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN))
			.andExpect(jsonPath("$.expiresIn").value(JWT_EXPIRATION.toSeconds()))
			.andExpect(jsonPath("$.refreshExpiresIn").value(REFRESH_TOKEN_EXPIRATION.toSeconds()));
		}

		@Test
		@DisplayName("Should return 400 when email is blank")
		void should_return_400_when_email_is_blank() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest("", PASSWORD);
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors[0].message").value(ValidationMessages.EMAIL_BLANK));
		}
		
		@Test
		@DisplayName("Should return 400 when email is invalid")
		void should_return_400_when_email_is_invalid() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest("invalid-email", PASSWORD);
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors[0].message").value(ValidationMessages.EMAIL_INVALID));
		}
		
		@Test
		@DisplayName("Should return 400 when password is invalid")
		void should_return_400_when_password_is_invalid() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest(EMAIL, "invalid-password");
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors[0].message").value(ValidationMessages.PASSWORD_INVALID));
		}
		
		@Test
		@DisplayName("Should return 409 when email already taken")
		void should_return_409_when_email_already_takend() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest(EMAIL, PASSWORD);
			
			when(service.register(request)).thenThrow(new BusinessException(ErrorCode.CREDENTIAL_TAKEN));
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value(ErrorCode.CREDENTIAL_TAKEN.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.CREDENTIAL_TAKEN.getMessage()));
		}
	}
	
	@Nested
	@DisplayName("POST /api/auth/refresh-token")
	class RefreshToken {
		
		@Test
		@DisplayName("Should return 200 when refresh token successfully")
		void should_return_200_when_refresh_token_successfully() throws JacksonException, Exception {
			RefreshTokenRequest request = new RefreshTokenRequest(RAW_TOKEN);
			AuthResponse response = new AuthResponse(
				ACCESS_TOKEN,
				REFRESH_TOKEN,
				JWT_EXPIRATION.toSeconds(),
				REFRESH_TOKEN_EXPIRATION.toSeconds()
			);
		
			when(service.refreshToken(request)).thenReturn(response);
			
			mockMvc.perform(
				post("/api/auth/refresh-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
			.andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN))
			.andExpect(jsonPath("$.expiresIn").value(JWT_EXPIRATION.toSeconds()))
			.andExpect(jsonPath("$.refreshExpiresIn").value(REFRESH_TOKEN_EXPIRATION.toSeconds()));
		}
		
		@Test
		@DisplayName("Should return 401 when refresh token failed")
		void should_return_401_when_refresh_token_failed() throws JacksonException, Exception {
			RefreshTokenRequest request = new RefreshTokenRequest(RAW_TOKEN);
		
			when(service.refreshToken(request)).thenThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID));
			
			mockMvc.perform(
				post("/api/auth/refresh-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isUnauthorized());
		}
	}
	
	@Nested
	@DisplayName("POST /api/auth/logout")
	class Logout {
		
		@Test
		@DisplayName("Should return 204 when logout successfully")
		void should_return_204_when_logout_successfully() throws JacksonException, Exception {
			RefreshTokenRequest request = new RefreshTokenRequest(RAW_TOKEN);
			
			mockMvc.perform(
				post("/api/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isNoContent());
			
			verify(service, times(1)).logout(request);
		}
		
		@Test
		@DisplayName("Should return 401 when logout failed")
		void should_return_401_when_logout_failed() throws JacksonException, Exception {
			RefreshTokenRequest request = new RefreshTokenRequest(RAW_TOKEN);
			
			doThrow(new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID))
				.when(service).logout(request);
			
			mockMvc.perform(
				post("/api/auth/logout")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_INVALID.name()));
		}
	}
}
