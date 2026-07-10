package com.vietsrepo.pricewatch.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

	@NotBlank(message = ValidationMessages.USERNAME_BLANK)
	String username,

	@NotBlank(message = ValidationMessages.PASSWORD_BLANK)
	String password
) {

}
