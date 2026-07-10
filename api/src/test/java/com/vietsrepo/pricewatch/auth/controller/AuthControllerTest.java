package com.vietsrepo.pricewatch.auth.controller;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

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
import com.vietsrepo.pricewatch.dto.LoginRequest;
import com.vietsrepo.pricewatch.dto.LoginResponse;
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
			LoginResponse response = new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN);
		
			when(service.login(request)).thenReturn(response);
			
			mockMvc.perform(
				post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
			.andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN));
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
			.andExpect(jsonPath("$.errors.username").value(ValidationMessages.USERNAME_BLANK));
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
			.andExpect(jsonPath("$.errors.password").value(ValidationMessages.PASSWORD_BLANK));
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
			.andExpect(jsonPath("$.errors.username").value(ValidationMessages.USERNAME_BLANK))
			.andExpect(jsonPath("$.errors.password").value(ValidationMessages.PASSWORD_BLANK));
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
			RegisterRequest request = new RegisterRequest(EMAIL, USERNAME, PASSWORD);
		
			when(service.register(request)).thenReturn(UUID.fromString(USER_ID));
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", "/api/users/" + USER_ID));
		}

		@Test
		@DisplayName("Should return 400 when email is blank")
		void should_return_400_when_email_is_blank() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest("", USERNAME, PASSWORD);
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors.email").value(ValidationMessages.EMAIL_BLANK));
		}
		
		@Test
		@DisplayName("Should return 400 when email is invalid")
		void should_return_400_when_email_is_invalid() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest("invalid-email", USERNAME, PASSWORD);
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors.email").value(ValidationMessages.EMAIL_INVALID));
		}
		
		@Test
		@DisplayName("Should return 400 when username is blank")
		void should_return_400_when_username_is_blank() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest(EMAIL, " ", PASSWORD);
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors.username").value(ValidationMessages.USERNAME_BLANK));
		}
		
		@Test
		@DisplayName("Should return 400 when password is invalid")
		void should_return_400_when_password_is_invalid() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest(EMAIL, USERNAME, "invalid-password");
			
			mockMvc.perform(
				post("/api/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.name()))
			.andExpect(jsonPath("$.message").value(ErrorCode.VALIDATION_FAILED.getMessage()))
			.andExpect(jsonPath("$.errors.password").value(ValidationMessages.PASSWORD_INVALID));
		}
		
		@Test
		@DisplayName("Should return 409 when email or username already taken")
		void should_return_409_when_email_or_username_already_takend() throws JacksonException, Exception {
			RegisterRequest request = new RegisterRequest(EMAIL, USERNAME, PASSWORD);
			
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
			LoginResponse response = new LoginResponse(ACCESS_TOKEN, REFRESH_TOKEN);
		
			when(service.refreshToken(request)).thenReturn(response);
			
			mockMvc.perform(
				post("/api/auth/refresh-token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.accessToken").value(ACCESS_TOKEN))
			.andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN));
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
