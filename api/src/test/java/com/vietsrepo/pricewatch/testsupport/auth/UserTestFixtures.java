package com.vietsrepo.pricewatch.testsupport.auth;

import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.EMAIL;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.PASSWORD;
import static com.vietsrepo.pricewatch.testsupport.auth.AuthTestConstants.USERNAME;

import com.vietsrepo.pricewatch.entity.User;
import com.vietsrepo.pricewatch.enums.Role;

public final class UserTestFixtures {

	public static User.UserBuilder defaultUserBuilder() {
		return User.builder()
				.email(EMAIL)
				.username(USERNAME)
				.password(PASSWORD)
				.role(Role.USER);
	}
}
