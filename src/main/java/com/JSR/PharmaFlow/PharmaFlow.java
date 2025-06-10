package com.JSR.PharmaFlow;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

//@SpringBootApplication
//@EnableJpaRepositories(basePackages = "com.JSR.PharmaFlow.Repository")
//@EntityScan(basePackages = "com.JSR.PharmaFlow.Entity")
//@ComponentScan (basePackages = "com.JSR.PharmaFlow")

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.JSR.PharmaFlow.Repository")
@EntityScan(basePackages = "com.JSR.PharmaFlow.Entity")
@ComponentScan(basePackages = {
		"com.JSR.PharmaFlow",
		"com.JSR.PharmaFlow.Controllers",
		"com.JSR.PharmaFlow.Services"
})
public class PharmaFlow {

	public static void main(String[] args) {
		SpringApplication.run(PharmaFlow.class, args);
	}

	@Bean
	public CommandLineRunner printAllMappings( ApplicationContext ctx) {
		return args -> {
			RequestMappingHandlerMapping mapping = ctx.getBean(RequestMappingHandlerMapping.class);
			mapping.getHandlerMethods().forEach((key, value) -> {
				System.out.println(key + " : " + value);
			});
		};
	}


}
