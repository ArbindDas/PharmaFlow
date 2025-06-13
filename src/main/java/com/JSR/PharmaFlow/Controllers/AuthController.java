package com.JSR.PharmaFlow.Controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.JSR.PharmaFlow.Enums.OAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.JSR.PharmaFlow.DTO.LoginRequest;
import com.JSR.PharmaFlow.DTO.SignUpRequest;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.Services.CustomUserDetailsService;
import com.JSR.PharmaFlow.Utils.JwtUtil;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import com.JSR.PharmaFlow.Enums.Role;
import org.springframework.web.bind.annotation.RequestMethod;
import com.JSR.PharmaFlow.DTO.Response;
import static com.JSR.PharmaFlow.Utility.RedisKeyCleanup.sanitizeKey;
import com.JSR.PharmaFlow.DTO.ForgotPasswordRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        allowCredentials = "true")
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {



    @Autowired
    @Qualifier("mapRedisTemplate")
    private RedisTemplate<String, Map<String, Object>> usersRedisTemplate;



    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    private final UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
            CustomUserDetailsService userDetailsService, UsersRepository usersRepository,
            PasswordEncoder passwordEncoder) {

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;


    }


    @GetMapping("/simple-test")
    public String test() {
        return "Working!";
    }



    @RequestMapping(value = "/hello", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> corsTest() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String jwt = jwtUtil.generateToken(userDetails.getUsername().trim());

            log.info("the jwt token generated from backend -> " + jwt );

            return ResponseEntity.ok(new Response.JwtResponse(
                    jwt,
                    userDetails.getUsername(),
                    userDetails.getAuthorities()));


        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            log.error("Authentication error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        try {
            if (usersRepository.existsByEmail(signUpRequest.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(new Response.ApiResponse(false, "Email is already in use!"));
            }

            if (usersRepository.existsByFullName(signUpRequest.getFullname())) {
                return ResponseEntity.badRequest()
                        .body(new Response.ApiResponse(false, "Fullname is already taken!"));
            }

            Users user = new Users();
            user.setFullName(signUpRequest.getFullname().trim());
            user.setEmail(signUpRequest.getEmail().trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRoles(Set.of(Role.USER));
            user.setAuthProvider( OAuthProvider.LOCAL);  // <-- Add this line

            Users savedUser = usersRepository.save(user);

            log.info("user saved successfully {}" + savedUser.getEmail() , savedUser.getFullName() , savedUser.getPassword() , savedUser.getRoles());

            return ResponseEntity.ok(new Response.ApiResponse(true, "User registered successfully"));

        } catch (DataIntegrityViolationException e) {
            log.error("Database error during registration: {}", e.getRootCause().getMessage(), e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Response.ApiResponse(false, "Registration conflict occurred: " + e.getRootCause().getMessage()));
        } catch (Exception e) {
            log.error("Registration error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response.ApiResponse(false, "Registration failed"));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        UserDetails userDetails = (UserDetails) principal;
        String email = userDetails.getUsername();
        String redisKey = "profile:" + sanitizeKey(email);

        // 1. Try to get from Redis
        Map<String, Object> cachedProfile = usersRedisTemplate.opsForValue().get(redisKey);
        if (cachedProfile != null) {
            return ResponseEntity.ok(cachedProfile);
        }

        // 2. If not found in Redis, build profile
        Map<String, Object> profile = new HashMap<>();
        profile.put("email", email);

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        profile.put("roles", roles);

        // 3. Store in Redis with TTL
        usersRedisTemplate.opsForValue().set(redisKey, profile, Duration.ofMinutes(5));

        return ResponseEntity.ok(profile);
    }



//    @PostMapping("/forgot-password")
//    public ResponseEntity<?> updateTheUser(@RequestBody Map<String, String> request) {
//        String email = request.get("email");
//
//        // Validate email format
//        if (email == null || email.isEmpty()) {
//            return ResponseEntity.badRequest().body("Email is required");
//        }
//        if (!email.matches("/^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$/")) {
//            return ResponseEntity.badRequest().body("Please enter a valid email address");
//        }
//
//        try {
//            // Check if user exists
//            Optional<Users> userOptional = usersRepository.findByEmail(email);
//
//            if (userOptional.isPresent()) {
//                Users user = userOptional.get();
//
//                // Generate JWT reset token (1 hour expiration)
//                String resetToken = jwtUtil.generatePasswordResetToken(user.getEmail());
//
//                // Store hashed token
//                user.setPasswordResetTokenHash(jwtUtil.hashToken(resetToken));
//                user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
//                usersRepository.save(user);
//
//                // In production: Send email
//                // emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
//
//                // For development: Log token
//                System.out.println("Reset token for " + email + ": " + resetToken);
//            }
//
//            // Always return the same success response
//            String responseBody = "If an account with " + email + " exists, you'll receive a password reset link. " +
//                    "Please check your inbox and spam folder. The link expires in 1 hour.";
//
//            return ResponseEntity.ok().body(responseBody);
//
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("An error occurred while processing your request");
//        }
//    }


    @PostMapping("/forgot-password")  // Make sure this matches your frontend
    public ResponseEntity<?> handleForgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            // Validate email format
            if (request.getEmail() == null || !request.getEmail().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest().body("Please enter a valid email address");
            }

            // Check if user exists
            Users user = usersRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate and save token
            String resetToken = jwtUtil.generatePasswordResetToken(user.getEmail());
            user.setPasswordResetTokenHash(jwtUtil.hashToken(resetToken));
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            usersRepository.save(user);

            // Log token for development
            log.info("Reset token for {}: {}", request.getEmail(), resetToken);

            return ResponseEntity.ok().body(
                    "If an account with " + request.getEmail() + " exists, you'll receive a reset link"
            );
        } catch (RuntimeException e) {
            return ResponseEntity.ok().body("If this email exists, a reset link was sent");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("An error occurred");
        }
    }



}
