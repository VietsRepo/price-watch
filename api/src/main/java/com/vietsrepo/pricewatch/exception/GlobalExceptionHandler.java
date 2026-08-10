package com.vietsrepo.pricewatch.exception;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vietsrepo.pricewatch.dto.ErrorResponse;
import com.vietsrepo.pricewatch.dto.ErrorResponse.FieldError;
import com.vietsrepo.pricewatch.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(BadCredentialsException ex,
			HttpServletRequest request) {
		String path = request.getRequestURI();
		
		log.warn("Authentication failed for request at {}: {}", path, ex.getMessage());

		return ResponseEntity.status(ErrorCode.INVALID_CREDENTIALS.getStatus())
				.body(buildErrorResponse(ErrorCode.INVALID_CREDENTIALS, path, null));
	}

	@ExceptionHandler(InternalAuthenticationServiceException.class)
	public ResponseEntity<ErrorResponse> handleInternalAuthError(InternalAuthenticationServiceException ex,
			HttpServletRequest request) {
		String path = request.getRequestURI();
		
		log.error("Internal error during authentication at {}: {}", path, ex.getMessage(), ex);

		return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
				.body(buildErrorResponse(ErrorCode.INTERNAL_ERROR, path, null));
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, HttpServletRequest request) {
		String path = request.getRequestURI();

		log.warn("BusinessException occurred: code={}, message={}, path={}", e.getErrorCode().name(),
				e.getErrorCode().getMessage(), path);

		return ResponseEntity.status(e.getErrorCode().getStatus())
				.body(buildErrorResponse(e.getErrorCode(), path, null));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e,
			HttpServletRequest request) {

		List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> new ErrorResponse.FieldError(
					fieldError.getField(),
					fieldError.getDefaultMessage()
				))
				.sorted(Comparator.comparing(ErrorResponse.FieldError::field))
				.toList();

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(buildErrorResponse(ErrorCode.VALIDATION_FAILED, request.getRequestURI(), errors));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {
		String path = request.getRequestURI();

		log.error("Unexpected error at {}: {}", path, e.getMessage(), e);

		return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus())
				.body(buildErrorResponse(ErrorCode.INTERNAL_ERROR, path, null));
	}

	private ErrorResponse buildErrorResponse(ErrorCode errorCode, String path, List<FieldError> errors) {
		return new ErrorResponse(
				errorCode.getStatus().getReasonPhrase(),
				errorCode.name(),
				errorCode.getMessage(),
				path,
				errors,
				LocalDateTime.now()
			);
	}
}
