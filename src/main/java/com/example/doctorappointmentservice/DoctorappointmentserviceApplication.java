package com.example.doctorappointmentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point. Bootstraps the Spring context, component scanning,
 * auto-configuration and the embedded web server for the Doctor Appointment
 * Service REST API.
 */
@SpringBootApplication
public class DoctorappointmentserviceApplication {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments passed through to Spring Boot
	 */
	public static void main(String[] args) {
		SpringApplication.run(DoctorappointmentserviceApplication.class, args);
	}

}
