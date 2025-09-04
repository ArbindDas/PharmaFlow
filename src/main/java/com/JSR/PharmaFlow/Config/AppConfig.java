package com.JSR.PharmaFlow.Config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Configuration


public class AppConfig {
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper ();
    }

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Add rate limiting interceptor
        restTemplate.setInterceptors( Collections.singletonList(( request, body, execution) -> {
            // Implement your rate limiting logic here
            return execution.execute(request, body);
        }));

        return restTemplate;
    }
}
