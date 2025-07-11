package com.JSR.PharmaFlow.Config;
import com.JSR.PharmaFlow.Filters.JwtFilter;
import com.JSR.PharmaFlow.Filters.OAuth2StateValidationFilter;
import com.JSR.PharmaFlow.oauth.CustomOAuth2UserService;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
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
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtAuthEntryPoint authEntryPoint;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    @Autowired
    public SpringSecurity( JwtFilter jwtFilter , CustomUserDetailsService userDetailsService , OAuth2SuccessHandler oAuth2LoginSuccessHandler , OAuth2SuccessHandler oAuth2SuccessHandler , CustomOAuth2UserService customOAuth2UserService , JwtAuthEntryPoint authEntryPoint , OAuth2FailureHandler oAuth2FailureHandler ) {
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
                .csrf( AbstractHttpConfigurer :: disable )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authEntryPoint)  // Handle 401 errors
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // Recommended for JWT
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints (React, Auth, Health checks)
                        .requestMatchers(
                                "/api/medicines/getMedicines",
                                "/api/medicines/test"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/medicines/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/medicines/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/medicines/").authenticated()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // CORS preflight
                        .requestMatchers(
                                "/", "/login**", "/oauth2/**",  // OAuth2 & React routes
                                "/api/auth/**",
                                "/api/public/**",
                                "/api/health/**",
                                "/api/auth/forgot-password",
                                "/api/files/**",
                                "/check/**",
                                "/api/ollama/**",
                                "/api/public/med/**"

                        ).permitAll()

                        .requestMatchers(
                                "/api/users/**",
                                "/api/prescription/**",
                                "/api/order/**",
                                "/api/order-item/**"
                        ).authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/oauth2/authorization")  // Google OAuth2 starts here
                        )
                        .redirectionEndpoint(redirect -> redirect
                                .baseUri("/login/oauth2/code/*")  // Google redirects here
                        )
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)  // Custom OAuth2 user handling
                        )
                        .successHandler(oAuth2SuccessHandler)  // JWT token generation after OAuth2 success
                        .failureHandler(oAuth2FailureHandler)
                )

                // JWT filter (before UsernamePasswordAuth)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // OAuth2 state validation (security)
                .addFilterBefore(oauth2StateValidationFilter(), OAuth2AuthorizationRequestRedirectFilter.class);

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder( ) {
        return new BCryptPasswordEncoder ( );
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

    @Bean
    public OAuth2StateValidationFilter oauth2StateValidationFilter( ) {
        return new OAuth2StateValidationFilter ( );
    }

}
