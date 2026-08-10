package com.vietsrepo.pricewatch.security;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.ACCESS_TOKEN;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vietsrepo.pricewatch.enums.ErrorCode;
import com.vietsrepo.pricewatch.service.JwtService;
import com.vietsrepo.pricewatch.testsupport.auth.UserTestFixtures;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

	@Mock
	private JwtService jwtService;
	
	@Mock
	private CustomUserDetailsService customUserDetailsService;
	
	@InjectMocks
	private JwtAuthFilter filter;

	@Mock
	private FilterChain filterChain;
	
	private final MockHttpServletRequest request = new MockHttpServletRequest();
	private final MockHttpServletResponse response = new MockHttpServletResponse();
	
	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("Should skip authentication when no Authorization header")
	void should_skip_when_no_auth_header() throws ServletException, IOException {
		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain, times(1)).doFilter(request, response);
	}
	
	@Test
	@DisplayName("Should skip authentication when no bearver prefix")
	void should_skip_when_no_bearer_prefix() throws ServletException, IOException {
		request.addHeader("Authorization", "Basic ");
		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain, times(1)).doFilter(request, response);
	}
	
	@Test
	@DisplayName("Should skip authentication when username is null")
	void should_skip_when_username_is_null() throws ServletException, IOException {
		request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
		when(jwtService.extractUserId(ACCESS_TOKEN)).thenReturn(null);
		
		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(customUserDetailsService, never()).loadUserByUsername(any());
		verify(filterChain, times(1)).doFilter(request, response);
	}
	
	@Test
	@DisplayName("Should not overwrite authentication when already authenticated")
	void should_skip_when_authentication_is_not_null() throws ServletException, IOException {
		request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
		when(jwtService.extractUserId(ACCESS_TOKEN)).thenReturn(USER_ID);
		CustomUserDetails userDetails = new CustomUserDetails(UserTestFixtures.defaultUserBuilder().build());
		
		Authentication existingAuth = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(existingAuth);
		
		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuth);
		verify(customUserDetailsService, never()).loadUserByUsername(any());
		verify(filterChain, times(1)).doFilter(request, response);
	}
	
	@Test
	@DisplayName("Should skip authentication when username is null & authentication is not null")
	void should_skip_when_username_is_null_and_authentication_is_not_null() throws ServletException, IOException {
		request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
	    when(jwtService.extractUserId(ACCESS_TOKEN)).thenReturn(null);
		
		CustomUserDetails userDetails = new CustomUserDetails(UserTestFixtures.defaultUserBuilder().build());
		
		Authentication existingAuth = new UsernamePasswordAuthenticationToken(
			userDetails, null, userDetails.getAuthorities()
		);
		SecurityContextHolder.getContext().setAuthentication(existingAuth);
		
		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuth);
		verify(customUserDetailsService, never()).loadUserByUsername(any());
		verify(filterChain, times(1)).doFilter(request, response);
	}
	
	@Test
	@DisplayName("Should authenticate when token is valid")
	void should_authenticate_when_token_valid() throws ServletException, IOException {
		request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
		CustomUserDetails userDetails = new CustomUserDetails(UserTestFixtures.defaultUserBuilder().build());
		
		when(jwtService.extractUserId(ACCESS_TOKEN)).thenReturn(USER_ID);
		when(customUserDetailsService.loadUserById(USER_ID)).thenReturn(userDetails);
		when(jwtService.isTokenValid(userDetails)).thenReturn(true);
		
		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getDetails()).isNotNull();
		verify(filterChain, times(1)).doFilter(request, response);
	}
	
	@Test
	@DisplayName("Should skip authentication when user is invalid")
	void should_skip_when_user_invalid() throws ServletException, IOException {
		request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
		CustomUserDetails userDetails = new CustomUserDetails(UserTestFixtures.defaultUserBuilder().build());
		
		when(jwtService.extractUserId(ACCESS_TOKEN)).thenReturn(USER_ID);
		when(customUserDetailsService.loadUserById(USER_ID)).thenReturn(userDetails);
		when(jwtService.isTokenValid(userDetails)).thenReturn(false);
		
		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain, times(1)).doFilter(request, response);
	}
	
	@Test
	@DisplayName("Should skip authentication when token expired")
	void should_skip_when_token_expired() throws ServletException, IOException {
		request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
		CustomUserDetails userDetails = new CustomUserDetails(UserTestFixtures.defaultUserBuilder().build());
		
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities()
			)
		);
		when(jwtService.extractUserId(ACCESS_TOKEN)).thenThrow(new ExpiredJwtException(null, null, "JWT expired"));
		
		filter.doFilterInternal(request, response, filterChain);

		assertThat(request.getAttribute("JWT_ERROR_CODE")).isEqualTo(ErrorCode.TOKEN_EXPIRED);
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain, times(1)).doFilter(request, response);
	}

	@Test
	@DisplayName("Should skip authentication when token is invalid")
	void should_skip_when_token_invalid() throws ServletException, IOException {
		request.addHeader("Authorization", "Bearer " + ACCESS_TOKEN);
		
		CustomUserDetails userDetails = new CustomUserDetails(UserTestFixtures.defaultUserBuilder().build());
		
		SecurityContextHolder.getContext().setAuthentication(
			new UsernamePasswordAuthenticationToken(
				userDetails, null, userDetails.getAuthorities()
			)
		);
		when(jwtService.extractUserId(ACCESS_TOKEN)).thenThrow(new JwtException("JWT invalid"));
		
		filter.doFilterInternal(request, response, filterChain);

		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
		verify(filterChain, times(1)).doFilter(request, response);
	}
}
