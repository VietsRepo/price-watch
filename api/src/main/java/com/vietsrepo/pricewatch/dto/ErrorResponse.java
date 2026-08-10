package com.vietsrepo.pricewatch.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

public record ErrorResponse(
	String error,
	String code,
	String message,
	String path,
	@JsonInclude(JsonInclude.Include.NON_NULL)
	List<FieldError> errors,
	LocalDateTime timestamp
) {
	public ErrorResponse(String error, String code, String message, String path, LocalDateTime timestamp) {
		this(error, code, message, path, null, timestamp);
	}
	
	public record FieldError(String field, String message) {}
}
