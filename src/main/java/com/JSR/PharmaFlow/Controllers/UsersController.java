package com.JSR.PharmaFlow.Controllers;


import com.JSR.PharmaFlow.Entity.Users;
import com.JSR.PharmaFlow.Exception.UserNotFoundException;
import com.JSR.PharmaFlow.Services.RedisService;
import com.JSR.PharmaFlow.Services.UsersService;
import com.JSR.PharmaFlow.Utility.RedisKeyCleanup;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.time.Duration;
import java.util.Optional;


import static com.JSR.PharmaFlow.Utility.RedisKeyCleanup.sanitizeKey;


@Slf4j
@RestController
@RequestMapping ( "/api/users" )
public class UsersController {

    @Autowired
    private RedisKeyCleanup redisKeyCleanup;

    @Autowired
    private RedisTemplate< String, Users > usersRedisTemplate;

    @Autowired
    private RedisService redisService;

    private final UsersService usersService;

    @Autowired
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }


    @GetMapping ( "/get-by-id/{id}" )
    public ResponseEntity< ? > getUserById(@PathVariable Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();
            log.info("Authenticated user: {}, Authorities: {}", authenticatedUser, authentication.getAuthorities());

            String redisKey = "user:" + id;
            Users cachedUser = usersRedisTemplate.opsForValue().get(redisKey);

            if (cachedUser != null) {
                log.info(" User with ID {} found in Redis cache", id);
                return ResponseEntity.ok(cachedUser);
            }

            Optional< Users > usersOptional = usersService.getUserById(id);
            if (usersOptional.isPresent()) {
                Users user = usersOptional.get();
                log.info(" User with ID {} fetched from DB", id);

                usersRedisTemplate.opsForValue().set(redisKey, user);
                log.debug("User with ID {} cached in Redis with key '{}'", id, redisKey);

                return ResponseEntity.ok(user);
            } else {
                log.warn(" User with ID {} not found in DB", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with ID " + id + " not found");
            }

        } catch (RuntimeException e) {
            log.error(" Error fetching user with ID {}: {}", id, e.getMessage(), e);
            throw new UserNotFoundException("User not found for ", id + e.getMessage());
        }
    }


    @GetMapping ( "/get-by-fullName/{username}" )
    public ResponseEntity< ? > getUserByFullName(@PathVariable String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();
            log.info("Authenticated user: {} is attempting to fetch user with username: {}", authenticatedUser, username);

            String redisKey = "user:" + sanitizeKey(username);
            Users cachedUser = usersRedisTemplate.opsForValue().get(redisKey);


            if (cachedUser != null) {
                log.info("User with username '{}' found in Redis cache", username);
                return ResponseEntity.ok(cachedUser);
            }

            Optional< Users > usersOptional = usersService.getUserByFullName(username);
            if (usersOptional.isPresent()) {
                Users user = usersOptional.get();
                log.info("User with username '{}' retrieved from database", username);

                usersRedisTemplate.opsForValue().set(redisKey, user, Duration.ofMinutes(5));
                log.debug("User cached in Redis with key '{}'", redisKey);

                return ResponseEntity.ok(user);
            } else {
                log.warn("User with username '{}' not found in database", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with username '" + username + "' not found");
            }
        } catch (RuntimeException e) {
            log.error("Error fetching user with username '{}': {}", username, e.getMessage(), e);
            throw new UserNotFoundException("User not found for ", username + e.getMessage());
        }
    }


    @GetMapping ( "/get-by-email" )
    public ResponseEntity< ? > getUserByEmail(@RequestParam String email) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();
            log.info("Authenticated user: {} is attempting to fetch user with email: {}", authenticatedUser, email);


            String safeEmailKey = sanitizeKey(email);
            String redisKey = "user:" + safeEmailKey;


            Users cachedUser = usersRedisTemplate.opsForValue().get(redisKey);
            if (cachedUser != null) {
                log.info("User with email '{}' found in Redis cache", email);
                return ResponseEntity.ok(cachedUser);
            }

            Optional< Users > optionalUser = usersService.getUserByEmail(email);
            if (optionalUser.isPresent()) {
                Users user = optionalUser.get();
                log.info("User with email '{}' retrieved from database", email);

                usersRedisTemplate.opsForValue().set(redisKey, user, Duration.ofMinutes(5));
                log.debug("User cached in Redis with key '{}'", redisKey);

                return ResponseEntity.ok(user);
            } else {
                log.warn("User with email '{}' not found in database", email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with email: " + email);
            }

        } catch (RuntimeException e) {
            log.error("Error fetching user with email '{}': {}", email, e.getMessage(), e);
            throw new UserNotFoundException("User not found for ", email + e.getMessage());
        }
    }


    @DeleteMapping ( "/delete-by-id/{id}" )
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


    @DeleteMapping ( "/delete-by-fullName/{username}" )
    public ResponseEntity< ? > deleteByFullName(@PathVariable String username) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String authenticatedUser = authentication.getName();
            log.info("Authenticated user: {} is attempting to delete user with username: {}", authenticatedUser, username);

            Optional< Users > usersOptional = usersService.getUserByFullName(username);

            if (usersOptional.isEmpty()) {
                log.warn("User with username {} not found in database", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with username: " + username);
            }

            Users users = usersOptional.get();

            boolean isDeleted = usersService.deleteUserByFullName(username);
            if (isDeleted) {
                log.info("User with username '{}' successfully deleted from database", username);

                // Delete all Redis keys related to this user using your helper method
                redisKeyCleanup.deleteFromRedis(users);

                return ResponseEntity.noContent().build();
            } else {
                log.warn("User with username '{}' not found in database", username);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with username: " + username);
            }

        } catch (RuntimeException e) {
            log.error("Error deleting user with username '{}': {}", username, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user with username: " + username);
        }
    }


    @PutMapping ( "/update" )
    public ResponseEntity< ? > updateUser(@RequestBody @Valid Users updatedUser) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            log.info("Authenticated user '{}' is attempting to update user: {}", currentUsername, updatedUser);

            ResponseEntity< ? > response = usersService.updateUsers(updatedUser, currentUsername);

            if (response.getStatusCode().is2xxSuccessful()) {
                Users savedUser = (Users) response.getBody();
                if (savedUser != null) {
                    redisService.updateUserCache(savedUser);
                    log.info("User cache updated for: {}", savedUser.getId());
                }
            }

            return response;

        } catch (RuntimeException e) {
            log.error("Failed to update user: {}", updatedUser, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user");
        }
    }

}
