package com.vietsrepo.pricewatch.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vietsrepo.pricewatch.dto.ErrorResponse;
import com.vietsrepo.pricewatch.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
	
	@ExceptionHandler({ BadCredentialsException.class, InternalAuthenticationServiceException.class })
	public ResponseEntity<ErrorResponse> handleAuthenticationException(Exception ex, HttpServletRequest request) {
		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponse(
						HttpStatus.UNAUTHORIZED.getReasonPhrase(),
						ErrorCode.INVALID_CREDENTIALS.name(),
						ErrorCode.INVALID_CREDENTIALS.getMessage(),
						request.getRequestURI(),
						LocalDateTime.now()
					)
				);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e,
			HttpServletRequest request) {
		String path = request.getRequestURI();

		log.warn("BusinessException occurred: code={}, message={}, path={}",
			e.getErrorCode().name(),
			e.getErrorCode().getMessage(),
			path
		);

		return ResponseEntity
				.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponse(
						HttpStatus.UNAUTHORIZED.getReasonPhrase(),
						e.getErrorCode().name(),
						e.getErrorCode().getMessage(),
						path,
						LocalDateTime.now()
					)
				);
	}
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e,
			HttpServletRequest request) {

		Map<String, String> errors = e.getBindingResult().getFieldErrors().stream()
				.collect(Collectors
				.toMap(
					FieldError::getField,
					FieldError::getDefaultMessage,
					(existing, duplicate) -> existing)
				);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorResponse(
						HttpStatus.BAD_REQUEST.getReasonPhrase(),
						ErrorCode.VALIDATION_FAILED.name(),
						ErrorCode.VALIDATION_FAILED.getMessage(),
						request.getRequestURI(),
						errors,
						LocalDateTime.now()
					)
				);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception e, HttpServletRequest request) {

		log.error("Unexpected error at {}: {}", request.getRequestURI(), e.getMessage(), e);

		return ResponseEntity
				.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorResponse(
					HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
					ErrorCode.INTERNAL_ERROR.name(),
					ErrorCode.INTERNAL_ERROR.getMessage(),
					request.getRequestURI(),
					LocalDateTime.now()
				));
	}
}
