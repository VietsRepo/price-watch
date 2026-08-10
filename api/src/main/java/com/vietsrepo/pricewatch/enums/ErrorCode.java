package com.vietsrepo.pricewatch.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
	
	// AUTH
	INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid username or password"),
	TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Authentication failed"),
	TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Authentication failed"),
	REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid refresh token"),
	REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "Refresh token revoked"),
	REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh token expired"),
	CREDENTIAL_TAKEN(HttpStatus.CONFLICT, "Email already taken"),
	
	// GENERAL
	VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
	
	private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
