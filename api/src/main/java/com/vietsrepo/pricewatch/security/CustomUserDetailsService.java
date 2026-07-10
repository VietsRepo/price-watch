package com.vietsrepo.pricewatch.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.vietsrepo.pricewatch.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository repository;

	@Override
	public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
		return repository.findByEmailOrUsername(emailOrUsername)
				.map(CustomUserDetails::new)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with identity: " + emailOrUsername));
	}

}
