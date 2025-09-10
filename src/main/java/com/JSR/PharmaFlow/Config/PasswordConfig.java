
package com.JSR.PharmaFlow.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfig {

    @Bean("bcryptPasswordEncoder")  // Give it a specific name
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}