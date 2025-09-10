package com.JSR.PharmaFlow.Controllers;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigTestController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @GetMapping("/test-config")
    public String testConfig() {
        return "Client ID: " + clientId;
    }
}