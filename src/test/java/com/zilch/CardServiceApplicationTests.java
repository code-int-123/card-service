package com.zilch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.UUID;

class CardServiceApplicationTests {

	@Test
	void contextLoads() {
		System.out.println(UUID.randomUUID());
	}

}
