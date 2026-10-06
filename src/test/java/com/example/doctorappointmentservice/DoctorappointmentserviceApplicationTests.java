package com.example.doctorappointmentservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

// Needs a real Postgres (Flyway runs on startup) plus DB_URL / DB_USERNAME / DB_PASSWORD / JWT_SECRET.
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = ".+")
@SpringBootTest
class DoctorappointmentserviceApplicationTests {

	@Test
	void contextLoads() {
	}

}
