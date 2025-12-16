package com.JSR.PharmaFlow.Controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.JSR.PharmaFlow.DTO.*;
import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Exception.UnauthorizedAccessException;
import com.JSR.PharmaFlow.Exception.UserNotFoundException;
import com.JSR.PharmaFlow.Services.*;
import io.jsonwebtoken.Claims;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.Utils.JwtUtil;
import java.util.concurrent.TimeUnit;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import com.JSR.PharmaFlow.Enums.Role;

import static com.JSR.PharmaFlow.Utility.RedisKeyCleanup.sanitizeKey;
import org.springframework.security.access.prepost.PreAuthorize;
import com.JSR.PharmaFlow.Utility.RedisKeyCleanup;

@RestController
@CrossOrigin(origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT ,  RequestMethod.OPTIONS},
        allowedHeaders = "*",
        allowCredentials = "true")
@Slf4j
@RequestMapping("/api/auth")
public class AuthController{


    @Autowired
    private RedisService redisService;


    @Autowired
    private UsersService usersService;

    @Autowired
    private RedisKeyCleanup redisKeyCleanup;


    private final UserLoginService userLoginService;


    private final EmailService emailService;

    @Qualifier ( "mapRedisTemplate" )
    private RedisTemplate < String, Object > usersRedisTemplate;


    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    private final UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserLoginService userLoginService, EmailService emailService, AuthenticationManager authenticationManager , JwtUtil jwtUtil ,
                          CustomUserDetailsService userDetailsService , UsersRepository usersRepository ,
                          PasswordEncoder passwordEncoder,
                          @Qualifier("mapRedisTemplate")RedisTemplate<String , Object>usersRedisTemplate
                          ){
        this.userLoginService = userLoginService;
        this.emailService = emailService;

        this.authenticationManager=authenticationManager;
        this.jwtUtil=jwtUtil;
        this.userDetailsService=userDetailsService;
        this.usersRepository=usersRepository;
        this.passwordEncoder=passwordEncoder;
        this.usersRedisTemplate = usersRedisTemplate;



    }


    @PostMapping("/test-generate-token")
    public ResponseEntity<?> testGenerateToken(@RequestBody Map<String, Object> request) {
        try {
            String username = (String) request.get("username");
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) request.get("roles");

            log.info("🔐 Testing token generation for: {} with roles: {}", username, roles);

            // Generate token with roles
            String token = jwtUtil.generateToken(username, roles);

            // Decode and show what's in the token
            String[] parts = token.split("\\.");
            String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));

            log.info("🔐 Generated token payload: {}", payloadJson);

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "decoded_payload", payloadJson,
                    "message", "Token generated successfully"
            ));

        } catch (Exception e) {
            log.error("❌ Error in test endpoint: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping ( "/roles" )
    public List < String > getAllRoles(){
        return Arrays.stream(Role.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }


    @GetMapping ( "/simple-test" )
    public String test(){
        return "Working!";
    }


    @RequestMapping ( value = "/hello", method = RequestMethod.OPTIONS )
    public ResponseEntity < Void > corsTest(){
        return ResponseEntity.ok().build();
    }




    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            log.info("🔐 Signin attempt for email: {}", loginRequest.getEmail());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Log authorities
            log.info("🔐 Authorities from authentication:");
            userDetails.getAuthorities().forEach(auth ->
                    log.info("   - {}", auth.getAuthority())
            );

            // Extract roles WITH ROLE_ prefix (IMPORTANT!)
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    // ❌ REMOVE this line: .map(auth -> auth.replace("ROLE_", ""))
                    .collect(Collectors.toList());

            log.info("🔐 Extracted roles for JWT (with ROLE_ prefix): {}", roles);

            // Generate token WITH roles
            String jwt = jwtUtil.generateToken(userDetails.getUsername().trim(), roles);

            log.info("🔐 JWT generated (first 50 chars): {}...",
                    jwt.substring(0, Math.min(50, jwt.length())));

            // Debug: Decode and print the generated token
            try {
                Claims claims = jwtUtil.getClaims(jwt);
                log.info("🔐 Decoded JWT claims: {}", claims);
                log.info("🔐 Roles in JWT: {}", claims.get("roles"));
            } catch (Exception e) {
                log.error("🔐 Failed to decode generated JWT", e);
            }

            // First login logic
            if (userLoginService.isFirstLogin(loginRequest.getEmail())) {
                try {
                    userLoginService.sendWelcomeEmail(loginRequest.getEmail(), userDetails.getUsername());
                    userLoginService.markAsLoggedIn(loginRequest.getEmail());
                } catch (Exception e) {
                    log.error("Welcome email failed", e);
                }
            }

            // Create response
            List<String> responseRoles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            log.info("🔐 Returning response with roles: {}", responseRoles);

            return ResponseEntity.ok(new Response.JwtResponse(
                    jwt,
                    userDetails.getUsername(),
                    responseRoles));

        } catch (BadCredentialsException e) {
            log.error("❌ Bad credentials for email: {}", loginRequest.getEmail());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        } catch (Exception e) {
            log.error("❌ Authentication error for email: {}", loginRequest.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }



    @PostMapping ( "/signup" )
    public ResponseEntity < ? > registerUser(@Valid @RequestBody SignUpRequest signUpRequest){
        try {
            if (usersRepository.existsByEmail(signUpRequest.getEmail())){
                return ResponseEntity.badRequest()
                        .body(new Response.ApiResponse(false , "Email is already in use!"));
            }

            System.out.println(signUpRequest.getEmail());


            if (usersRepository.existsByFullName(signUpRequest.getFullname())){
                return ResponseEntity.badRequest()
                        .body(new Response.ApiResponse(false , "Fullname is already taken!"));
            }

            Users user=new Users();
            user.setFullName(signUpRequest.getFullname().trim());
            user.setEmail(signUpRequest.getEmail().trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            user.setRoles(Set.of(Role.USER));
            user.setAuthProvider(OAuthProvider.LOCAL);  // <-- Add this line

            Users savedUser=usersRepository.save(user);


            // Clear the users cache after new user is added
            usersRedisTemplate.delete("all_users");
            log.info("Cleared users cache after new registration");


            log.info("user saved successfully {}"+savedUser.getEmail() , savedUser.getFullName() , savedUser.getPassword() , savedUser.getRoles());

            return ResponseEntity.ok(new Response.ApiResponse(true , "User registered successfully"));

        } catch( DataIntegrityViolationException e ){
            log.error("Database error during registration: {}" , e.getRootCause().getMessage() , e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new Response.ApiResponse(false , "Registration conflict occurred: "+e.getRootCause().getMessage()));
        } catch( Exception e ){
            log.error("Registration error" , e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response.ApiResponse(false , "Registration failed"));
        }
    }








    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            // Validate email format
            if (request.getEmail() == null || !request.getEmail().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return ResponseEntity.badRequest().body(
                        Map.of("message", "Please enter a valid email address", "type", "error")
                );
            }

            // Check if user exists
            Optional<Users> userOptional = usersRepository.findByEmail(request.getEmail());

            // Always return the same message for security (prevent email enumeration)
            if (userOptional.isEmpty()) {
                log.info("Password reset requested for non-existent email: {}", request.getEmail());
                return ResponseEntity.ok().body(
                        Map.of("message", "If an account with that email exists, you'll receive a reset link", "type", "success")
                );
            }

            Users user = userOptional.get();

            // Generate unique token with timestamp to ensure uniqueness
            String uniquePayload = user.getEmail() + ":" + System.currentTimeMillis();
            String resetToken = jwtUtil.generatePasswordResetToken(uniquePayload);

            // Update user with new token
            user.setPasswordResetTokenHash(jwtUtil.hashToken(resetToken));
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            usersRepository.save(user);

            // TODO: Send email with reset token (remove logging in production)
            log.info("Reset token generated for: {}", request.getEmail());
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);

            return ResponseEntity.ok().body(
                    Map.of("message", "If an account with that email exists, you'll receive a reset link", "type", "success")
            );

        } catch (Exception e) {
            log.error("Error in forgot password for email: {}", request.getEmail(), e);
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "An error occurred. Please try again.", "type", "error")
            );
        }
    }



    @GetMapping ( "/profile" )
    public ResponseEntity < ? > getUserProfile(Authentication authentication){
        System.out.println("=== START PROFILE REQUEST ===");

        if (authentication==null || ! authentication.isAuthenticated()){
            System.out.println("Unauthorized - no authentication");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error" , "Unauthorized"));
        }

        Object principal=authentication.getPrincipal();
        System.out.println("Principal class: "+principal.getClass().getName());

        if (! ( principal instanceof UserDetails userDetails )){
            System.out.println("Unauthorized - principal not UserDetails");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error" , "Unauthorized"));
        }

        String email=userDetails.getUsername();
        System.out.println("User email: "+email);

        String redisKey="profile:"+sanitizeKey(email);
        System.out.println("Redis key: "+redisKey);

        Object cached=usersRedisTemplate.opsForValue().get(redisKey);
        System.out.println("Cached data: "+cached);

        if (cached instanceof Map < ?, ? > map){
            System.out.println("Returning cached profile with keys: "+map.keySet());
            return ResponseEntity.ok(map);
        }

        Optional < Users > optionalUser=usersRepository.findByEmail(email);
        System.out.println("User exists in DB: "+optionalUser.isPresent());

        if (optionalUser.isEmpty()){
            System.out.println("User not found in database");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error" , "User not found"));
        }

        Users user=optionalUser.get();
        System.out.println("User entity fields:");
        System.out.println("ID: "+user.getId());
        System.out.println("Email: "+user.getEmail());
        System.out.println("Full Name: "+user.getFullName());
        System.out.println("Auth Provider: "+user.getAuthProvider());
        System.out.println("Created At: "+user.getCreatedAt());

        Map < String, Object > profile=new HashMap <>();
        profile.put("id" , user.getId());
        profile.put("email" , user.getEmail());
        profile.put("full_name" , user.getFullName());
        profile.put("auth_provider" , user.getAuthProvider());
        profile.put("created_at" , user.getCreatedAt());
        profile.put("roles" , userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        System.out.println("Profile being cached: "+profile);

        usersRedisTemplate.opsForValue().set(redisKey , profile , Duration.ofMinutes(5));
        System.out.println("Profile cached successfully");

        System.out.println("=== END PROFILE REQUEST ===");
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
                log.info("Returning {} users from cache", cachedList.size());
                return ResponseEntity.ok(cachedList);
            }

            log.info("Cache miss - querying database");
            List<Users> users = (List<Users>) usersService.getAllUsers();

            if (!users.isEmpty()) {
                log.info("Fetched {} users from DB", users.size());

                // ✅ Apply Merge Sort by username (you can change to email/id/etc.)
                Users[] arr = users.toArray(new Users[0]);
                mergeSort(arr, 0, arr.length - 1);
                List<Users> sortedUsers = Arrays.asList(arr);

                usersRedisTemplate.opsForValue().set(redisKey, sortedUsers, 2, TimeUnit.MINUTES);
                log.info("Cached {} users with key {}", sortedUsers.size(), redisKey);

                return ResponseEntity.ok(Map.of("status", "success", "data", sortedUsers));
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error fetching users: {}", e.getMessage(), e);
            return new ResponseEntity<>("Failed to fetch users. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private void mergeSort(Users[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right) / 2;

            mergeSort(arr, left, mid);
            mergeSort(arr, mid + 1, right);

            merge(arr, left, mid, right);
        }
    }

    private void merge(Users[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        Users[] L = new Users[n1];
        Users[] R = new Users[n2];

        for (int i = 0; i < n1; i++) L[i] = arr[left + i];
        for (int j = 0; j < n2; j++) R[j] = arr[mid + 1 + j];

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            // ✅ Sort by username (ignore case)
            if (L[i].getId().compareTo(R[j].getId()) <= 0) {
                arr[k++] = L[i++];
            } else {
                arr[k++] = R[j++];
            }
        }

        while (i < n1) arr[k++] = L[i++];
        while (j < n2) arr[k++] = R[j++];
    }



    @DeleteMapping ( "/users/{userId}" )
    public ResponseEntity < ? > deleteUserByIdUser(@PathVariable Long id){
        try {
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser=authentication.getName();
            log.info("Authenticated user: {} is attempting to delete user with ID: {}" , authenticatedUser , id);

            Optional < Users > usersOptional=usersService.getUserById(id);

            if (usersOptional.isEmpty()){
                log.warn("User with ID {} not found in database" , id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: "+id);
            }

            Users users=usersOptional.get();

            boolean isDeleted=usersService.deleteUserById(id);

            if (isDeleted){
                log.info("User with ID {} successfully deleted from database" , id);
                redisKeyCleanup.deleteFromRedis(users);


                return ResponseEntity.noContent().build();
            } else{
                log.warn("User with ID {} not found in database" , id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: "+id);
            }

        } catch( RuntimeException e ){
            log.error("Error deleting user with ID {}: {}" , id , e.getMessage() , e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user with ID: "+id);
        }


    }


    @PutMapping ( "/admin/users/{id}" )
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity < ? > adminUpdateUser(
            @PathVariable Long id ,
            @RequestBody @Valid UserUpdateDTO updatedUser ,
            BindingResult bindingResult ,
            Authentication authentication){  // Add Authentication parameter

        try {
            // Validate ID match
            if (! id.equals(updatedUser.getId())){
                return ResponseEntity.badRequest()
                        .body(Map.of("status" , "error" , "message" , "ID mismatch"));
            }

            // Validate input
            if (bindingResult.hasErrors()){
                Map < String, String > errors=bindingResult.getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                FieldError::getField ,
                                fieldError -> fieldError.getDefaultMessage()!=null ?
                                        fieldError.getDefaultMessage() : "Validation error"
                        ));
                return ResponseEntity.badRequest()
                        .body(Map.of("status" , "error" , "errors" , errors));
            }

            // Get current admin's username from authentication
            String adminUsername=authentication.getName();

            // Pass both DTO and admin username
            Users savedUser=usersService.adminUpdateUserByDTO(updatedUser , adminUsername);

            // Update Redis cache
            try {
                redisService.updateUserCache(savedUser);

                // 2. INVALIDATE the all_users cache completely
                usersRedisTemplate.delete("all_users");
                log.info("Invalidated all_users cache after user update");
                log.info("Admin updated user ID: {}" , savedUser.getId());
            } catch( Exception e ){
                log.error("Failed to update Redis cache for user ID: {}" , savedUser.getId() , e);
            }

            return ResponseEntity.ok(Map.of(
                    "status" , "success" ,
                    "data" , savedUser
            ));

        } catch( RuntimeException e ){
            log.error("Admin user update failed" , e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status" , "error" , "message" , e.getMessage()));
        }
    }


    @PutMapping ( "/users/{id}" ) // More RESTful path
    public ResponseEntity < ? > updateUser(@PathVariable Long id ,
                                           @RequestBody @Valid UserUpdateDTO updatedUser ,
                                           BindingResult bindingResult){
        try {

            if (! id.equals(updatedUser.getId())){
                return ResponseEntity.badRequest()
                        .body(Map.of("status" , "error" , "message" , "ID mismatch"));
            }
            // Validation handling
            if (bindingResult.hasErrors()){
                Map < String, String > errors=bindingResult.getFieldErrors()
                        .stream()
                        .collect(Collectors.toMap(
                                FieldError::getField ,
                                fieldError -> fieldError.getDefaultMessage()!=null ?
                                        fieldError.getDefaultMessage() : "Validation error"
                        ));
                return ResponseEntity.badRequest()
                        .body(Map.of("status" , "error" , "errors" , errors));
            }

            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String currentUsername=authentication.getName();

            Users savedUser=usersService.updateUserByDTO(updatedUser , currentUsername);

            // Update Redis cache
            try {
                redisService.updateUserCache(savedUser);
                log.info("Redis cache updated for user ID: {}" , savedUser.getId());
            } catch( Exception e ){
                log.error("Failed to update Redis cache for user ID: {}" , savedUser.getId() , e);
                // Continue even if Redis fails - database is source of truth
            }

            return ResponseEntity.ok(Map.of(
                    "status" , "success" ,
                    "data" , savedUser
            ));

        } catch( RuntimeException e ){
            log.error("User update failed" , e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status" , "error" , "message" , e.getMessage()));
        }
    }


    @DeleteMapping ( "/admin/users/{id}" )
    @PreAuthorize ( "hasRole('ADMIN')" )
    public ResponseEntity < ? > deleteUserById(@PathVariable ( "id" ) Long userId){
        try {
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            if (authentication==null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
            }

            String authenticatedUsername=authentication.getName();
            log.info("Authenticated user: {} is attempting to delete user with ID: {}" , authenticatedUsername , userId);

            Optional < Users > currentUserOptional=usersService.getUserByEmail(authenticatedUsername);
            if (currentUserOptional.isEmpty()){
                log.warn("Authenticated user {} not found in database" , authenticatedUsername);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated properly");
            }

            Users currentUser=currentUserOptional.get();
            UserUpdateDTO requestingUser=convertToUserUpdateDTO(currentUser);

            Optional < Users > targetUserOptional=usersService.getUserById(userId);
            if (targetUserOptional.isEmpty()){
                log.warn("User with ID {} not found in database" , userId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with ID: "+userId);
            }

            boolean isDeleted=usersService.isAdmindeleteUserById(userId , requestingUser);

            if (isDeleted){
                log.info("User with ID {} successfully deleted by admin {}" , userId , authenticatedUsername);
                redisKeyCleanup.deleteFromRedis(targetUserOptional.get());
                return ResponseEntity.noContent().build();
            } else{
                log.warn("Failed to delete user with ID {}" , userId);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to delete user with ID: "+userId);
            }

        } catch( UnauthorizedAccessException e ){
            log.warn("Unauthorized deletion attempt: {}" , e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch( UserNotFoundException e ){
            log.warn("User not found: {}" , e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch( RuntimeException e ){
            log.error("Error deleting user with ID {}: {}" , userId , e.getMessage() , e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting user with ID: "+userId);
        }
    }

    // Helper method to convert Users to UserUpdateDTO
    private UserUpdateDTO convertToUserUpdateDTO(Users user){
        UserUpdateDTO dto=new UserUpdateDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRoles(user.getRoles());
        dto.setAuthProvider(user.getAuthProvider());
        return dto;
    }
}

