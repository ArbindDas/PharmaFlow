package com.JSR.PharmaFlow.Config;


import com.JSR.PharmaFlow.Filters.JwtFilter;
import com.JSR.PharmaFlow.Services.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
public class SpringSecurity {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthEntryPoint authEntryPoint;

    @Autowired
    public SpringSecurity( CustomUserDetailsService userDetailsService, JwtFilter jwtFilter ) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity http ) throws Exception {
        http
                .csrf( AbstractHttpConfigurer::disable )
                .cors( cors -> cors.configurationSource( corsConfigurationSource() ) ) // Enable CORS
                .exceptionHandling( exception -> exception.authenticationEntryPoint( authEntryPoint ) )
                .sessionManagement( session -> session.sessionCreationPolicy( SessionCreationPolicy.STATELESS ) )
                .authorizeHttpRequests( auth -> auth
                        .requestMatchers( HttpMethod.OPTIONS, "/**" ).permitAll() // Allow preflight
                        .requestMatchers("/api/auth/signup", "/api/auth/signin").permitAll()
                        .requestMatchers( "/api/auth/profile" ).authenticated()
                        .requestMatchers( "/api/public/**" ).permitAll()
                        .requestMatchers( HttpMethod.POST, "/api/public/**" ).permitAll()
                        .requestMatchers( "/api/health/**" ).permitAll()
                        .requestMatchers( "/api/test/**" ).permitAll()
                        .requestMatchers( "/api/users/**" ).authenticated()
                        .requestMatchers( "/api/prescription/**" ).authenticated()
                        .requestMatchers( "/api/order-item/**" ).authenticated()
                        .requestMatchers( "/api/order/**" ).authenticated()
                        .requestMatchers( "/api/medicine/**" ).authenticated()
                        .requestMatchers( "/api/admin/**" ).hasRole( "ADMIN" )


                        .anyRequest().authenticated() )

                .addFilterBefore( jwtFilter, UsernamePasswordAuthenticationFilter.class )

                .httpBasic( Customizer.withDefaults() );

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder( ) {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager( HttpSecurity httpSecurity ) throws Exception {
        AuthenticationManagerBuilder authBuilder = httpSecurity.getSharedObject( AuthenticationManagerBuilder.class );
        authBuilder.userDetailsService( userDetailsService ).passwordEncoder( passwordEncoder() );
        return authBuilder.build();
    }

    // CORS Configuration
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins( List.of("http://localhost:5173")); // React's URL
//        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // Allow OPTIONS
//        configuration.setAllowedHeaders(List.of("*"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

    @Bean
    CorsConfigurationSource corsConfigurationSource( ) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins( Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "http://your-host-ip:5173"
        ) );
        config.setAllowedMethods( List.of( "*" ) );
        config.setAllowedHeaders( List.of( "*" ) );
        config.setAllowCredentials( true );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration( "/**", config );
        return source;
    }
}
