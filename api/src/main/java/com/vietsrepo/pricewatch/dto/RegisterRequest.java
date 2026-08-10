package com.vietsrepo.pricewatch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(

	@NotBlank(message = ValidationMessages.EMAIL_BLANK)
	@Email(message = ValidationMessages.EMAIL_INVALID)
	String email,

	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[0-9])[A-Za-z0-9@$!%*?&]{8,32}$",
		message = ValidationMessages.PASSWORD_INVALID
	)
	String password
) {
}
