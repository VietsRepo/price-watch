package com.vietsrepo.pricewatch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.vietsrepo.pricewatch.config.PostgresTestContainerConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgresTestContainerConfig.class)
class PriceWatchApplicationTests {

	@Test
	void contextLoads() {
	}

}
