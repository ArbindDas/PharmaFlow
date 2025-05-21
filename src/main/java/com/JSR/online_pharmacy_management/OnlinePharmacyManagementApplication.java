package com.JSR.online_pharmacy_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.JSR.online_pharmacy_management.Entity")  // Replace with your actual entity package
@EnableJpaRepositories(basePackages = "com.JSR.online_pharmacy_management.Repository")  // Replace with your actual repository package
public class OnlinePharmacyManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlinePharmacyManagementApplication.class, args);
	}

}
