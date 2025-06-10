package com.JSR.PharmaFlow.Controllers;

import com.JSR.PharmaFlow.DTO.LoginDto;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Services.CustomUserDetailsService;
import com.JSR.PharmaFlow.Services.UsersService;
import com.JSR.PharmaFlow.Utils.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/public")
public class PublicController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService customUsersDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    public final UsersService usersService;

    @Autowired
    public PublicController(UsersService usersService) {
        this.usersService = usersService;
    }


    @PostConstruct
    public void init() {
        System.out.println(">>>>> PublicController initialized");
    }


    @GetMapping("/test")
    public String getTest(){
        return  "jai shree ram";
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createNewUsers(@RequestBody @Valid Users users) {
        try {
            log.info("Creating new user: {}", users);
            boolean isUserCreated = usersService.saveNewUser(users);

            if (isUserCreated) {
                log.info("User created successfully: {}", users);
                return new ResponseEntity<>(true, HttpStatus.CREATED);
            } else {
                log.warn("User creation failed for: {}", users);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

        } catch (RuntimeException e) {
            log.error("Error occurred while creating user: {}", e.getMessage(), e);
            return new ResponseEntity<>("An error occurred while creating the user.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody @Valid Users users) {
        try {
            boolean savedUser = usersService.saveNewUser(users);
            if (savedUser) {
                return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (RuntimeException e) {
            log.error("Signup error: {}", e.getMessage(), e);
            return new ResponseEntity<>("Signup failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> loginUser(@RequestBody @Valid LoginDto loginDto) {
        try {
            log.info("Login attempt for email: {}", loginDto.email());

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDto.email(),
                            loginDto.password()
                    )
            );

            UserDetails userDetails = customUsersDetailsService.loadUserByUsername(loginDto.email());
            String jwt = jwtUtil.generateToken(userDetails.getUsername().trim());

            return ResponseEntity.ok(Map.of(
                    "token", jwt,
                    "message", "Login successful"
            ));

        } catch (BadCredentialsException e) {
            log.error("Authentication failed for email: {}", loginDto.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Something went wrong"));
        }
    }
}
