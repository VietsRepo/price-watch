package com.vietsrepo.pricewatch.properties;

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

	private long expiration;
	
	private short refreshTokenExpiration;

	private String secret;
}
