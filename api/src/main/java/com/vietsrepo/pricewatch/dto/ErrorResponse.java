package com.vietsrepo.pricewatch.dto;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
	String error,
    String code,
    String message,
    String path,
    Map<String, String> errors,
    LocalDateTime timestamp
) {
	public ErrorResponse(String error, String code, String message, String path, LocalDateTime timestamp) {
		this(error, code, message, path, null, timestamp);
	}
}
