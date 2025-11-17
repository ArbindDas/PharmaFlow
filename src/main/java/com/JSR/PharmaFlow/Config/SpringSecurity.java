package com.JSR.PharmaFlow.Config;
import com.JSR.PharmaFlow.Filters.JwtFilter;
import com.JSR.PharmaFlow.Services.CustomOAuth2UserService;
import com.JSR.PharmaFlow.Services.CustomUserDetailsService;
import com.JSR.PharmaFlow.oauth.OAuth2FailureHandler;
import com.JSR.PharmaFlow.oauth.OAuth2SuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;
import java.util.List;

import com.JSR.PharmaFlow.Config.PasswordConfig;

@Configuration
@EnableWebSecurity
public class SpringSecurity {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthEntryPoint authEntryPoint;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Autowired
    public SpringSecurity( JwtFilter jwtFilter , CustomUserDetailsService userDetailsService ,
                           OAuth2SuccessHandler oAuth2LoginSuccessHandler ,
                            OAuth2SuccessHandler oAuth2SuccessHandler ,
                           CustomOAuth2UserService customOAuth2UserService ,
                           JwtAuthEntryPoint authEntryPoint , OAuth2FailureHandler oAuth2FailureHandler ) {
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
        this.authEntryPoint = authEntryPoint;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - should come FIRST
                        .requestMatchers(
                                "/api/medicines/getMedicines",
                                "/api/medicines/test",
                                "/api/medicines/add",
                                "/login/oauth2/**",
                                "/oauth2/**",
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/health/**",
                                "/api/auth/forgot-password",
                                "/api/files/**",
                                "/check/**",
                                "/api/ollama/**",
                                "/api/public/med/**",
                                "/api/reset/**", // ADD THIS - Allow password reset endpoints without authentication
                                "/api/payment/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // ✅ Allow anyone to delete medicines
//                        .requestMatchers(HttpMethod.DELETE, "/api/medicines/**").permitAll()


                        // Role-based and authenticated endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/orders/**").authenticated()



                        // Any other request - should ALWAYS come LAST
                        .anyRequest().authenticated()


                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .defaultSuccessUrl("http://localhost:3000/oauth-success", true)
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/oauth2/**",
                                "/login/**",
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/health/**",
                                "/api/orders/**" , // ADD THIS LINE to ignore CSRF for orders API
                                "/api/payment/**",
                                "/api/medicines/**", // ✅ Add this line
                                "/api/reset/**", // ADD THIS - Allow password reset endpoints without authentication
                                "/api/reset/**"
                        )
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder( ) {
        return new BCryptPasswordEncoder (    );
    }

    @Bean
    public AuthenticationManager authenticationManager( HttpSecurity httpSecurity ) throws Exception {
        AuthenticationManagerBuilder authBuilder = httpSecurity.getSharedObject ( AuthenticationManagerBuilder.class );
        authBuilder.userDetailsService ( userDetailsService ).passwordEncoder ( passwordEncoder ( ) );
        return authBuilder.build ( );
    }
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Allowed origins (React app)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));

        // Allowed methods (include PUT for direct S3 uploads if needed)
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"
        ));

        // Allowed headers (add S3-specific headers)
        config.setAllowedHeaders(Arrays.asList(
                "*",  // Or specify explicitly:
                "Authorization",
                "Content-Type",
                "Content-Disposition",
                "x-amz-acl",
                "x-amz-meta-*"

        ));

        config.setAllowCredentials(true);
        config.addExposedHeader("Authorization");
        config.setExposedHeaders(List.of("Set-Cookie")); // Expose cookies
        config.setExposedHeaders(List.of("X-CSRF-TOKEN")); // Expose CSRF token

        // Exposed headers (add S3-specific headers)
        config.setExposedHeaders(Arrays.asList(
                "Cross-Origin-Opener-Policy",
                "Cross-Origin-Embedder-Policy",
                "ETag",  // Important for S3
                "x-amz-version-id",
                "x-amz-request-id"
        ));

        // Additional S3-specific settings
        config.setMaxAge(3600L);  // Cache preflight requests for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }



}
