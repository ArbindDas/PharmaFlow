package com.JSR.PharmaFlow.Controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Services.RedisService;
import com.JSR.PharmaFlow.Services.UsersService;
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
import org.springframework.web.bind.annotation.*;
import com.JSR.PharmaFlow.DTO.LoginRequest;
import com.JSR.PharmaFlow.DTO.SignUpRequest;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.Services.CustomUserDetailsService;
import com.JSR.PharmaFlow.Utils.JwtUtil;
import java.util.concurrent.TimeUnit;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import com.JSR.PharmaFlow.Enums.Role;
import com.JSR.PharmaFlow.DTO.Response;
import static com.JSR.PharmaFlow.Utility.RedisKeyCleanup.sanitizeKey;
import com.JSR.PharmaFlow.DTO.ForgotPasswordRequest;
import com.JSR.PharmaFlow.Utility.RedisKeyCleanup;

@RestController
@CrossOrigin(origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT ,  RequestMethod.OPTIONS},
        allowedHeaders = "*",
        allowCredentials = "true")
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private RedisService redisService;


    @Autowired
    private UsersService usersService;

    @Autowired
    private RedisKeyCleanup redisKeyCleanup;

    @Autowired
    @Qualifier("mapRedisTemplate")
    private RedisTemplate<String, Object> usersRedisTemplate;




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


        Object cached = usersRedisTemplate.opsForValue().get(redisKey);
        if (cached instanceof Map<?, ?> map) {
            return ResponseEntity.ok(map);
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

    @GetMapping("/get-all-users")
    public ResponseEntity<?> getAllUsers() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();

            log.info("Authenticated user {} is attempting to fetch all users", authenticatedUser);

            String redisKey = "all_users";

            Object cached = usersRedisTemplate.opsForValue().get(redisKey);
            if (cached instanceof List<?> cachedList) {
                return ResponseEntity.ok(cachedList);
            }
            List<?> users = usersService.getAllUsers();
            if (!users.isEmpty()) {
                log.info("Fetched {} users from DB", users.size());

                usersRedisTemplate.opsForValue().set(redisKey, users, 2, TimeUnit.MINUTES);
//                return ResponseEntity.ok(users);
                return ResponseEntity.ok(Map.of("status", "success", "data", users));

            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return new ResponseEntity<>("Failed to fetch users. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @DeleteMapping ( "/users/{userId}" )
    public ResponseEntity< ? > deleteUserById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();
            log.info("Authenticated user: {} is attempting to delete user with ID: {}", authenticatedUser, id);

            Optional< Users > usersOptional = usersService.getUserById(id);

            if (usersOptional.isEmpty()) {
                log.warn("User with ID {} not found in database", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + id);
            }

            Users users = usersOptional.get();

            boolean isDeleted = usersService.deleteUserById(id);

            if (isDeleted) {
                log.info("User with ID {} successfully deleted from database", id);
                redisKeyCleanup.deleteFromRedis(users);


                return ResponseEntity.noContent().build();
            } else {
                log.warn("User with ID {} not found in database", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: " + id);
            }

        } catch (RuntimeException e) {
            log.error("Error deleting user with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user with ID: " + id);
        }


    }


    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@RequestBody @Valid Users updatedUser) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            System.out.println("Auth: " + currentUsername);

            log.info(" Authenticated user '{}' is attempting to update user: {}", currentUsername, updatedUser);

            // Log essential user info
            log.debug(" Incoming update for email: {}", updatedUser.getEmail());
            log.debug(" Incoming update for ID: {}", updatedUser.getId());

            ResponseEntity<?> response = usersService.updateUsers(updatedUser, currentUsername);

            if (response.getStatusCode().is2xxSuccessful()) {
                Users savedUser = (Users) response.getBody();
                if (savedUser != null) {
                    log.info(" User updated successfully: ID={}, Email={}", savedUser.getId(), savedUser.getEmail());

                    try {
                        redisService.updateUserCache(savedUser);
                        log.info(" Redis cache updated for user ID: {}", savedUser.getId());
                    } catch (Exception redisEx) {
                        log.error(" Redis cache update failed for user ID: {}", savedUser.getId(), redisEx);
                    }
                } else {
                    log.warn("  User update returned null savedUser");
                }
            } else {
                log.warn("️ User update failed with status: {}", response.getStatusCode());
            }

            return ResponseEntity.ok(
                    Map.of(
                            "status", "success",
                            "data", usersService.updateUsers(updatedUser, currentUsername)
                    )
            );

        } catch (RuntimeException e) {
            log.error(" Exception while updating user: {}", updatedUser, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user");
        }
    }



}
