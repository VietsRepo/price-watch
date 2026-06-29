package com.vietsrepo.pricewatch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(

	@NotBlank(message = "Email cannot be blank")
	@Email(message = "Email is invalid")
	String email,
	
	@NotBlank(message = "Username cannot be blank")
	String username,

	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&])[A-Za-z0-9@$!%*?&]{8,32}$",
		message = """
		Password must meet the following requirements:
		- 8 to 32 characters
		- At least one uppercase and one lowercase letter
		- At least one number
		- At least one special character"""
	)
	String password
) {
}
