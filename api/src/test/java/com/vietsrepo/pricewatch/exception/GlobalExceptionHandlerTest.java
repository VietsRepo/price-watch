package com.vietsrepo.pricewatch.exception;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.vietsrepo.pricewatch.dto.ErrorResponse;
import com.vietsrepo.pricewatch.dto.ValidationMessages;
import com.vietsrepo.pricewatch.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
	
	@Mock
	private HttpServletRequest request;
	
	@Mock
	private MethodArgumentNotValidException exception;

	@Mock
	private BindingResult bindingResult;

	private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

	@Test
	@DisplayName("Should return 500 when InternalAuthenticationServiceException occurs")
	void should_return_500_when_internal_authentication_service_exception_occurs() {
		String path = "/api/auth/login";
		when(request.getRequestURI()).thenReturn(path);

		ResponseEntity<ErrorResponse> response = handler.handleInternalAuthError(
			new InternalAuthenticationServiceException("db error"), request
		);

		assertErrorResponse(ErrorCode.INTERNAL_ERROR, path, response);
	}
	
	@Test
	@DisplayName("Should keep first error when a field has multiple validation errors")
	void should_keep_first_error_when_field_has_multiple_errors() {
		String path = "/api/auth/register";
		String emailField = "email";
		when(request.getRequestURI()).thenReturn(path);
		
		FieldError firstError = new FieldError("registerRequest", emailField, ValidationMessages.EMAIL_BLANK);
		FieldError secondError = new FieldError("registerRequest", emailField, ValidationMessages.EMAIL_INVALID);

		when(exception.getBindingResult()).thenReturn(bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(firstError, secondError));

		ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception, request);

		assertThat(response.getStatusCode()).isEqualTo(ErrorCode.VALIDATION_FAILED.getStatus());
		assertThat(response.getBody().errors())
			.containsEntry(emailField, ValidationMessages.EMAIL_BLANK)
			.hasSize(1);
	}
	
	@Test
	@DisplayName("Should return 500 when Exception occurs")
	void should_return_500_when_generic_exception_occurs() {
		String path = "/api/auth/login";
		when(request.getRequestURI()).thenReturn(path);

		ResponseEntity<ErrorResponse> response = handler.handleGenericException(new Exception(), request);

		assertErrorResponse(ErrorCode.INTERNAL_ERROR, path, response);
	}

	private void assertErrorResponse(ErrorCode expectedErrorCode, String path, ResponseEntity<ErrorResponse> response) {
		assertThat(response.getStatusCode()).isEqualTo(expectedErrorCode.getStatus());
		assertThat(response.getBody())
			.extracting(ErrorResponse::error, ErrorResponse::code, ErrorResponse::message, ErrorResponse::path)
			.containsExactly(
				ErrorCode.INTERNAL_ERROR.getStatus().getReasonPhrase(),
				ErrorCode.INTERNAL_ERROR.name(),
				ErrorCode.INTERNAL_ERROR.getMessage(),
				path
			);
	}
}
