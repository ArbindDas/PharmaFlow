package com.JSR.PharmaFlow.Controllers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.JSR.PharmaFlow.DTO.*;
import com.JSR.PharmaFlow.Enums.OAuthProvider;
import com.JSR.PharmaFlow.Exception.UnauthorizedAccessException;
import com.JSR.PharmaFlow.Exception.UserNotFoundException;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Repository.UsersRepository;
import com.JSR.PharmaFlow.Services.CustomUserDetailsService;
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

    @Autowired
    @Qualifier ( "mapRedisTemplate" )
    private RedisTemplate < String, Object > usersRedisTemplate;


    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final CustomUserDetailsService userDetailsService;

    private final UsersRepository usersRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager , JwtUtil jwtUtil ,
                          CustomUserDetailsService userDetailsService , UsersRepository usersRepository ,
                          PasswordEncoder passwordEncoder){

        this.authenticationManager=authenticationManager;
        this.jwtUtil=jwtUtil;
        this.userDetailsService=userDetailsService;
        this.usersRepository=usersRepository;
        this.passwordEncoder=passwordEncoder;


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

    @PostMapping ( "/signin" )
    public ResponseEntity < ? > authenticateUser(@Valid @RequestBody LoginRequest loginRequest){
        try {
            Authentication authentication=authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail() ,
                            loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails=( UserDetails ) authentication.getPrincipal();


            String jwt=jwtUtil.generateToken(userDetails.getUsername().trim());

            log.info("the jwt token generated from backend -> "+jwt);

            return ResponseEntity.ok(new Response.JwtResponse(
                    jwt ,
                    userDetails.getUsername() ,
                    userDetails.getAuthorities()));


        } catch( BadCredentialsException e ){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error" , "Invalid email or password"));
        } catch( Exception e ){
            log.error("Authentication error" , e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error" , "Authentication failed"));
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


    @PostMapping ( "/forgot-password" )  // Make sure this matches your frontend
    public ResponseEntity < ? > handleForgotPassword(@RequestBody ForgotPasswordRequest request){
        try {
            // Validate email format
            if (request.getEmail()==null || ! request.getEmail().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")){
                return ResponseEntity.badRequest().body("Please enter a valid email address");
            }

            // Check if user exists
            Users user=usersRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Generate and save token
            String resetToken=jwtUtil.generatePasswordResetToken(user.getEmail());
            user.setPasswordResetTokenHash(jwtUtil.hashToken(resetToken));
            user.setPasswordResetTokenExpiry(LocalDateTime.now().plusHours(1));
            usersRepository.save(user);

            // Log token for development
            log.info("Reset token for {}: {}" , request.getEmail() , resetToken);

            return ResponseEntity.ok().body(
                    "If an account with "+request.getEmail()+" exists, you'll receive a reset link"
            );
        } catch( RuntimeException e ){
            return ResponseEntity.ok().body("If this email exists, a reset link was sent");
        } catch( Exception e ){
            return ResponseEntity.internalServerError().body("An error occurred");
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


    @GetMapping ( "/get-all-users" )
    public ResponseEntity < ? > getAllUsers(){
        try {
            Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser=authentication.getName();

            log.info("Authenticated user {} is attempting to fetch all users" , authenticatedUser);

            String redisKey="all_users";


            Object cached=usersRedisTemplate.opsForValue().get(redisKey);
            if (cached instanceof List < ? > cachedList){
                log.info("Returning {} users from cache" , cachedList.size());
                return ResponseEntity.ok(cachedList);
            }


            log.info("Cache miss - querying database");
            List < ? > users=usersService.getAllUsers();
            if (! users.isEmpty()){
                log.info("Fetched {} users from DB" , users.size());

                usersRedisTemplate.opsForValue().set(redisKey , users , 2 , TimeUnit.MINUTES);
                log.info("Cached {} users with key {}" , users.size() , redisKey);
                return ResponseEntity.ok(Map.of("status" , "success" , "data" , users));

            } else{
                return new ResponseEntity <>(HttpStatus.NOT_FOUND);
            }
        } catch( Exception e ){
            log.error("Error fetching users: {}" , e.getMessage() , e);
            return new ResponseEntity <>("Failed to fetch users. Please try again later." , HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

