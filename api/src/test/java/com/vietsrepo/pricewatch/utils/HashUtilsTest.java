package com.vietsrepo.pricewatch.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HashUtilsTest {
	
	private static final String INPUT_A = "value-a";
	private static final String INPUT_B = "value-b";

	@Test
	@DisplayName("Should return consistent hash for same input")
	void should_return_consistent_hash_for_same_input() {
		assertThat(HashUtils.sha256(INPUT_A)).isEqualTo(HashUtils.sha256(INPUT_A));
	}

	@Test
	@DisplayName("Should return different hash for different input")
	void should_return_different_hash_for_different_input() {
		assertThat(HashUtils.sha256(INPUT_A)).isNotEqualTo(HashUtils.sha256(INPUT_B));
	}

	@Test
	@DisplayName("Should return known SHA-256 hash for a fixed input")
	void should_return_known_hash_for_fixed_input() {
		assertThat(HashUtils.sha256("hello"))
			.isEqualTo("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
	}
}
