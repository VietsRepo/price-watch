package com.vietsrepo.pricewatch.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.vietsrepo.pricewatch.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@NoArgsConstructor
@Getter
@Setter
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(updatable = false)
	private UUID id;

	private String email;

	private String username;

	private String password;

	@Enumerated(EnumType.STRING)
	private Role role;

	private boolean enabled = true;

	@Column(name = "account_locked")
	private boolean accountLocked;

	@Column(name = "created_at", updatable = false, insertable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", insertable = false)
	private LocalDateTime updatedAt;
	
	public User(String email, String username, String password, Role role) {
		this.email = email;
		this.username = username;
		this.password = password;
		this.role = role;
	}
}
