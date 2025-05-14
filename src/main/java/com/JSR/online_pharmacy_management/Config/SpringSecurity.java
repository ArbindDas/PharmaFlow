package com.JSR.online_pharmacy_management.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SpringSecurity {


    @Bean
    public SecurityFilterChain securityFilterChain( HttpSecurity httpSecurity ) throws Exception{
        httpSecurity
                .csrf ( AbstractHttpConfigurer :: disable )
                .authorizeHttpRequests ( auth -> auth
                        .requestMatchers ( "/api/user/**" ).authenticated ()
                        .requestMatchers ( "/api/public/**" ).hasRole ( "ADMIN" )
                        .requestMatchers ( "/api/prescription/**" ).authenticated ()
                        .requestMatchers ( "/api/order-item/**" ).authenticated ()
                        .requestMatchers ( "/api/order/**" ).authenticated ()
                        .requestMatchers ( "/api/medicine/**" ).authenticated ()
                        .requestMatchers ( "/api/login/**" ).permitAll ()
                        .requestMatchers ( "/api/admin/**" ).hasRole ( "ADMIN" )
                        .anyRequest ().permitAll ()
                );

         return httpSecurity.build ();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder (  );
    }


//    @Bean
//    public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception{
//        AuthenticationManagerBuilder authBuilder = httpSecurity.getSharedObject ( AuthenticationManagerBuilder.class );
//        authBuilder.userDetailsService ( usersDetailsService ).passwordEncoder ( passwordEncoder () );
//        return authBuilder.build ();
//    }

}
