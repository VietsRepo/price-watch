package com.vietsrepo.pricewatch.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vietsrepo.pricewatch.dto.LoginRequest;
import com.vietsrepo.pricewatch.dto.LoginResponse;
import com.vietsrepo.pricewatch.dto.RefreshTokenRequest;
import com.vietsrepo.pricewatch.dto.RegisterRequest;
import com.vietsrepo.pricewatch.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;

	@PostMapping("/login")
	public LoginResponse login (@RequestBody LoginRequest request) {
		return authService.login(request);
	}
	
	@PostMapping("/register")
	public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
		UUID userId = authService.register(registerRequest);
		URI location = URI.create("/api/users/" + userId);

		return ResponseEntity.created(location).build();
	}
	
	@PostMapping("/refresh-token")
	public LoginResponse refreshToken (@RequestBody RefreshTokenRequest request) {
		return authService.refreshToken(request);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
		authService.logout(request);
		
		return ResponseEntity.noContent().build();
	}
}
