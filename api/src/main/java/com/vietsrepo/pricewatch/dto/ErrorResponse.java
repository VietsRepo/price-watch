package com.vietsrepo.pricewatch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(
	String error,
	String code,
	String message,
	String path,
	@JsonInclude(JsonInclude.Include.NON_NULL)
	Map<String, String> errors,
	LocalDateTime timestamp
) {
	public ErrorResponse(String error, String code, String message, String path, LocalDateTime timestamp) {
		this(error, code, message, path, null, timestamp);
	}
}
