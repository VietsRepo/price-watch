package com.vietsrepo.pricewatch.exception;

import com.vietsrepo.pricewatch.enums.ErrorCode;

public class BusinessException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final ErrorCode code;

	public BusinessException(ErrorCode code) {
		super(code.getMessage());
		this.code = code;
	}

	public ErrorCode getErrorCode() {
		return code;
	}
}
