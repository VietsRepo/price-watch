package com.vietsrepo.pricewatch.security;

import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.vietsrepo.pricewatch.entity.User;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

	private static final long serialVersionUID = 1L;
	private final User user;
	
	public User getUser() {
		return user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority(user.getRole().name()));
	}

	@Override
	public @Nullable String getPassword() {
		return user.getPassword();
	}

	public String getEmail() {
		return user.getEmail();
	}

	@Override
	public String getUsername() {
		return user.getUsername();
	}
	
	@Override
	public boolean isEnabled() {
		return user.isEnabled();
	}

	@Override
	public boolean isAccountNonLocked() {
		return !user.isAccountLocked();
	}
}
