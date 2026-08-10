package com.vietsrepo.pricewatch.dto;

public record AuthResponse(String accessToken, String refreshToken, long expiresIn, long refreshExpiresIn) {

}
