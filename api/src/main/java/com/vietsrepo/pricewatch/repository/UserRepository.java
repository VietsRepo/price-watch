package com.vietsrepo.pricewatch.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vietsrepo.pricewatch.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

	@Query("SELECT u FROM User u WHERE u.email = :identifier OR u.username = :identifier")
	Optional<User> findByEmailOrUsername(@Param("identifier") String identifier);
	
	boolean existsByEmail(String email);
}
