package com.JSR.PharmaFlow.Config;

import com.JSR.PharmaFlow.Services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringSecurity {


    private final CustomUserDetailsService userDetailsService;

    @Autowired
    public SpringSecurity ( CustomUserDetailsService userDetailsService ) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain ( HttpSecurity httpSecurity ) throws Exception {
        httpSecurity
                .csrf (AbstractHttpConfigurer :: disable)
                .sessionManagement (session -> session.sessionCreationPolicy (SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests (auth -> auth
                        .requestMatchers ("/api/public/**").permitAll ()
                        .requestMatchers ("/api/login/**").permitAll ()
                        .requestMatchers ("/api/health/**").permitAll ()
                        .requestMatchers ("/api/test/**").permitAll ()
                        .requestMatchers ("/api/users/**").authenticated ()
                        .requestMatchers ("/api/prescription/**").authenticated ()
                        .requestMatchers ("/api/order-item/**").authenticated ()
                        .requestMatchers ("/api/order/**").authenticated ()
                        .requestMatchers ("/api/medicine/**").authenticated ()
                        .requestMatchers ("/api/admin/**").hasRole ("ADMIN")
                        .anyRequest ().permitAll ()
                );
                 httpSecurity.httpBasic (Customizer.withDefaults()); // ✅ Enables Basic Authentication explicitly

        return httpSecurity.build ();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder ( ) {
        return new BCryptPasswordEncoder ();
    }


    @Bean
    public AuthenticationManager authenticationManager ( HttpSecurity httpSecurity ) throws Exception {
        AuthenticationManagerBuilder authBuilder = httpSecurity.getSharedObject (AuthenticationManagerBuilder.class);
        authBuilder.userDetailsService (userDetailsService).passwordEncoder (passwordEncoder ());
        return authBuilder.build ();
    }

}
