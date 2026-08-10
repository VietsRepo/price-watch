package com.vietsrepo.pricewatch.security;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.vietsrepo.pricewatch.dto.ErrorResponse;
import com.vietsrepo.pricewatch.enums.ErrorCode;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class JwtAuthEntryPointTest {

	private final JsonMapper jsonMapper = new JsonMapper();
	private final JwtAuthEntryPoint entryPoint = new JwtAuthEntryPoint(jsonMapper);
	private final MockHttpServletRequest request = new MockHttpServletRequest();
	private final MockHttpServletResponse response = new MockHttpServletResponse();
	private static final String BAD_CREDENTIALS_MESSAGE = "Bad credentials";
	
	@Nested
	@DisplayName("commence()")
	class Commence {

		@Test
		@DisplayName("Should use forward request URI attribute when present")
		void should_use_forward_attribute_when_present() throws IOException, ServletException {
			String path = "/api/users/" + USER_ID;
			request.setAttribute(RequestDispatcher.FORWARD_REQUEST_URI, path);
			
			entryPoint.commence(request, response, new BadCredentialsException(BAD_CREDENTIALS_MESSAGE));
			
			assertUnauthorizedResponse();
			assertThat(readBody(response).path()).isEqualTo(path);
		}
		
		@Test
		@DisplayName("Should use request URI when no forward attribute present")
		void should_use_request_uri_when_no_forward_attribute() throws IOException, ServletException {
			String path = "/api/auth/login";
			request.setRequestURI(path);
			
			entryPoint.commence(request, response, new BadCredentialsException(BAD_CREDENTIALS_MESSAGE));
			
			assertUnauthorizedResponse();
			assertThat(readBody(response).path()).isEqualTo(path);
		}
		
		@Test
		@DisplayName("Should use JWT_ERROR_CODE attribute when present")
		void should_use_jwt_error_code_attribute_when_present() throws IOException, ServletException {
			request.setAttribute("JWT_ERROR_CODE", ErrorCode.TOKEN_EXPIRED);
			
			entryPoint.commence(request, response, new BadCredentialsException(BAD_CREDENTIALS_MESSAGE));
			
			assertUnauthorizedResponse();
			assertThat(readBody(response).code()).isEqualTo(ErrorCode.TOKEN_EXPIRED.name());
		}
		
		@Test
		@DisplayName("Should default to TOKEN_INVALID when JWT_ERROR_CODE attribute is absent")
		void should_default_to_token_invalid_when_attribute_absent() throws IOException, ServletException {
			entryPoint.commence(request, response, new BadCredentialsException(BAD_CREDENTIALS_MESSAGE));
			
			assertUnauthorizedResponse();
			assertThat(readBody(response).code()).isEqualTo(ErrorCode.TOKEN_INVALID.name());
		}

		private void assertUnauthorizedResponse() {
			assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON_VALUE + ";charset=" + StandardCharsets.UTF_8);
			assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	private ErrorResponse readBody(MockHttpServletResponse response) {
		return jsonMapper.readValue(response.getContentAsByteArray(), ErrorResponse.class);
	}
}