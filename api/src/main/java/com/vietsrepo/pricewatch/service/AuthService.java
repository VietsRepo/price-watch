package com.vietsrepo.pricewatch.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.vietsrepo.pricewatch.dto.LoginRequest;
import com.vietsrepo.pricewatch.dto.LoginResponse;
import com.vietsrepo.pricewatch.dto.RefreshTokenRequest;
import com.vietsrepo.pricewatch.dto.RegisterRequest;
import com.vietsrepo.pricewatch.entity.RefreshToken;
import com.vietsrepo.pricewatch.entity.User;
import com.vietsrepo.pricewatch.enums.ErrorCode;
import com.vietsrepo.pricewatch.enums.Role;
import com.vietsrepo.pricewatch.exception.BusinessException;
import com.vietsrepo.pricewatch.repository.UserRepository;
import com.vietsrepo.pricewatch.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenService refreshTokenService;
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder encoder;

	public LoginResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(
					request.username(), request.password()
			)
		);

		CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

		String accessToken = jwtService.generateToken(user.getUsername()); // NOSONAR
		String refreshToken = refreshTokenService.create(user.getUser());

		return new LoginResponse(accessToken, refreshToken);
	}
	
	public UUID register(RegisterRequest request) {
		if (userRepository.existsByEmailOrUsername(request.email(), request.username())) {
			throw new BusinessException(ErrorCode.CREDENTIAL_TAKEN);
		}
		
		User user = new User(
			request.email(),
			request.username(),
			encoder.encode(request.password()),
			Role.USER
		);
		
		User userSaved = userRepository.save(user);
		
		return userSaved.getId();
	}

	public LoginResponse refreshToken(RefreshTokenRequest request) {
		// Verify old refreshToken
		RefreshToken oldToken = refreshTokenService.verify(request.refreshToken());
		
		User user = oldToken.getUser();

		// Rotate: revoke old token
		refreshTokenService.revoke(request.refreshToken());

		String accessToken = jwtService.generateToken(user.getUsername());
		String refreshToken = refreshTokenService.create(user);

		return new LoginResponse(accessToken, refreshToken);
	}
	
	public void logout(RefreshTokenRequest request) {
		refreshTokenService.revoke(request.refreshToken());
	}
}
