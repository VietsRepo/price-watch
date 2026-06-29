package com.vietsrepo.pricewatch.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vietsrepo.pricewatch.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByEmailOrUsername(String email, String username);
	
	boolean existsByEmailOrUsername(String email, String username);
}
