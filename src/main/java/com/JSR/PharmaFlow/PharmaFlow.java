package com.JSR.PharmaFlow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.JSR.PharmaFlow.Repository")
@EntityScan(basePackages = {
        "com.JSR.PharmaFlow.Entity",      // If you have entities here
        "com.JSR.PharmaFlow.Events"       // Add this for JwtToken
})
public class PharmaFlow {
    public static void main(String[] args) {
        SpringApplication.run(PharmaFlow.class, args);
    }
}