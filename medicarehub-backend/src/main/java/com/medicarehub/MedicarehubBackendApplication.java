package com.medicarehub; // or com.medicarehub.backend

import com.medicarehub.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MedicarehubBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(MedicarehubBackendApplication.class, args);
	}

	@Bean
	CommandLineRunner run(AuthService authService) {
		return args -> {
			authService.seedRoles();
		};
	}
}