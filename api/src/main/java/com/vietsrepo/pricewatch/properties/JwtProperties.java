package com.vietsrepo.pricewatch.properties;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {

	private String issuer;

	private Duration expiration;
	
	private Duration refreshTokenExpiration;

	private String secret;
}
