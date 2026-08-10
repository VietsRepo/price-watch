package com.vietsrepo.pricewatch.dto;

public final class ValidationMessages {

	private ValidationMessages() {
	}

	public static final String EMAIL_BLANK = "Email cannot be blank";
	public static final String EMAIL_INVALID = "Email is invalid";
	public static final String USERNAME_BLANK = "Username cannot be blank";
	public static final String PASSWORD_BLANK = "Password cannot be blank";
	public static final String PASSWORD_INVALID = """
			Password must meet the following requirements:
			- 8 to 32 characters
			- At least one lowercase letter
			- At least one number""";
}
