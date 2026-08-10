package com.vietsrepo.pricewatch.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vietsrepo.pricewatch.dto.AuthResponse;
import com.vietsrepo.pricewatch.dto.LoginRequest;
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
	public AuthResponse login (@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}
	
	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
		return authService.register(registerRequest);
	}
	
	@PostMapping("/refresh-token")
	public AuthResponse refreshToken (@RequestBody RefreshTokenRequest request) {
		return authService.refreshToken(request);
	}
	
	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
		authService.logout(request);
		
		return ResponseEntity.noContent().build();
	}
}
