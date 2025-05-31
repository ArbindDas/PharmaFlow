package com.JSR.PharmaFlow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.JSR.online_pharmacy_management.Entity")  // Replace with your actual entity package
@EnableJpaRepositories(basePackages = "com.JSR.PharmaFlow.Repository")  // Replace with your actual repository package
public class PharmaFlow {

	public static void main(String[] args) {
		SpringApplication.run(PharmaFlow.class, args);
	}

}
